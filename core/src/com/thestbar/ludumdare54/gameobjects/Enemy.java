package com.thestbar.ludumdare54.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.thestbar.ludumdare54.GameApp;
import com.thestbar.ludumdare54.managers.SoundManager;
import com.thestbar.ludumdare54.screens.GameScreen;
import com.thestbar.ludumdare54.utils.Box2DUtils;
import com.thestbar.ludumdare54.utils.Constants;
import java.util.HashMap;
import java.util.Map;

public class Enemy {
    public GameApp game;
    private SoundManager soundManager;
    public Body body;
    public int enemyType;
    private final Animation<TextureRegion> restAnimation;
    private final Animation<TextureRegion> attackAnimation;
    private final Animation<TextureRegion> flippedRestAnimation;
    private final Animation<TextureRegion> flippedAttackAnimation;
    private float stateTime;
    public EnemyState enemyState;
    public static int enemies = 0;
    private final float ANIMATION_FRAME_DURATION = 0.3f;
    public float range;
    public static Map<String, Enemy> enemiesMap = new HashMap<>();
    public static Array<Enemy> enemiesArray = new Array<>();
    public float healthPoints;
    private final float maxHealthPoints;
    public float damage;
    private final TextureRegion[] healthBarTextures;
    private float timeSinceLastRangeAttack;
    private final World world;
    private boolean isAttackToPlayerEnabled;
    private final boolean isEnemyRanged;
    private float timeSinceLastMeleeAttack;

    public Enemy(GameApp game, World world, int enemyType, int x, int y, float range, SoundManager soundManager) {
        this.game = game;
        this.soundManager = soundManager;
        this.enemyType = enemyType;
        this.range = range;
        this.world = world;
        switch (enemyType) {
            case 0:
                healthPoints = 200;
                damage = 10;
                isEnemyRanged = false;
                break;
            case 1:
                healthPoints = 150;
                damage = 15;
                isEnemyRanged = false;
                break;
            case 2:
                healthPoints = 80;
                damage = 20;
                isEnemyRanged = false;
                break;
            case 3:
                healthPoints = 60;
                damage = 35;
                isEnemyRanged = true;
                break;
            default:
                healthPoints = 0;
                damage = 0;
                isEnemyRanged = false;
                break;
        }
        maxHealthPoints = healthPoints;

        String id = "enemy" + (++enemies);
        enemiesMap.put(id, this);

        body = Box2DUtils.createBox(world,x, y, 10, 14, BodyDef.BodyType.DynamicBody);
        body.getFixtureList().get(0).setDensity(1);
        body.getFixtureList().get(0).setFriction(0);
        body.getFixtureList().get(0).getFilterData().categoryBits = Constants.BIT_ENEMY;
        body.getFixtureList().get(0).getFilterData().maskBits = Constants.BIT_GROUND;
        body.getFixtureList().get(0).setUserData(id);

        // Create enemy sensor (hit box)
        String sensorId = "enemy_sensor" + enemies;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(16 / Constants.PPM, 8 / Constants.PPM, new Vector2(0, 0), 0);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Constants.BIT_ENEMY_SENSOR;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER | Constants.BIT_WEAPON_SENSOR;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef).setUserData(sensorId);
        shape.dispose();

        // Create enemy attack sensor
        String attackSensorId = "enemy_attack_sensor" + enemies;
        shape = new PolygonShape();
        shape.setAsBox(10 / Constants.PPM, 8 / Constants.PPM, new Vector2(0, 0), 0);
        fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Constants.BIT_ENEMY_ATTACK_SENSOR;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef).setUserData(attackSensorId);
        shape.dispose();

        TextureRegion[][] tmp = TextureRegion.split(game.assetManager
                .get("spritesheets/ld54-enemies-Sheet.png", Texture.class), 16, 16);

        TextureRegion[][] tmpFlipped = TextureRegion.split(game.assetManager
                .get("spritesheets/ld54-enemies-Sheet.png", Texture.class), 16, 16);

        for (int i = 0; i < 4; ++i) {
            tmpFlipped[2 * enemyType][i].flip(true, false);
            tmpFlipped[2 * enemyType + 1][i].flip(true, false);
        }

        restAnimation = new Animation<>(ANIMATION_FRAME_DURATION, tmp[2 * enemyType + 1]);
        attackAnimation = new Animation<>(ANIMATION_FRAME_DURATION, tmp[2 * enemyType]);

        flippedRestAnimation = new Animation<>(ANIMATION_FRAME_DURATION, tmpFlipped[2 * enemyType + 1]);
        flippedAttackAnimation = new Animation<>(ANIMATION_FRAME_DURATION, tmpFlipped[2 * enemyType]);

        enemyState = EnemyState.REST;

        // Create health bar
        healthBarTextures = TextureRegion.split(game.assetManager
                .get("spritesheets/ld54-healrthbar-Sheet.png", Texture.class), 16, 16)[0];

        timeSinceLastRangeAttack = 0;
        isAttackToPlayerEnabled = false;
        timeSinceLastMeleeAttack = 0;
    }

    public enum EnemyState {
        REST,
        ATTACK
    }

    public void render(SpriteBatch batch, boolean flip) {
        stateTime += Gdx.graphics.getDeltaTime();
        if (enemyType == 3) {
            // Has range attack
            timeSinceLastRangeAttack += Gdx.graphics.getDeltaTime();
            if (timeSinceLastRangeAttack >= 10 * ANIMATION_FRAME_DURATION) {
                timeSinceLastRangeAttack = 0;
                new Fireball(game, world, (int) (body.getPosition().x * Constants.PPM),
                        (int) (body.getPosition().y * Constants.PPM), flip, damage);
            }
        }
        if (healthPoints == 0) {
            Enemy.enemiesArray.removeValue(this, true);
            GameScreen.bodiesToBeDeleted.add(body);
            return;
        }
        if (enemyState == EnemyState.ATTACK && stateTime > 4 * ANIMATION_FRAME_DURATION) {
            enemyState = EnemyState.REST;
            stateTime = 0f;
        }
        Animation<TextureRegion> currentWeaponAnimation;
        if (enemyState == EnemyState.REST) {
            currentWeaponAnimation = (flip) ? flippedRestAnimation : restAnimation;
        } else {
            currentWeaponAnimation = (flip) ? flippedAttackAnimation : attackAnimation;
        }
        TextureRegion currentFrame = currentWeaponAnimation.getKeyFrame(stateTime, true);
        float x = body.getPosition().x * Constants.PPM - 8f;
        float y = body.getPosition().y * Constants.PPM - 8f;
        // Draw health bar
        int textureIndex = (int) (12 - (healthPoints / maxHealthPoints) * 12);
        batch.begin();
        batch.draw(currentFrame, x, y);
        batch.draw(healthBarTextures[textureIndex], x, y + 16);
        batch.end();

        // Check if player is close and damage them
        if (isAttackToPlayerEnabled && !isEnemyRanged) {
            timeSinceLastMeleeAttack += Gdx.graphics.getDeltaTime();
            if (timeSinceLastMeleeAttack >= ANIMATION_FRAME_DURATION) {
                timeSinceLastMeleeAttack = 0;
                int floorOfStateTime = (int) Math.floor(stateTime);
                float decimalOfStateTime = stateTime - floorOfStateTime;
                floorOfStateTime %= 4;
                float animationStateTimeInLoop = floorOfStateTime + decimalOfStateTime;
                boolean attackPlayer = animationStateTimeInLoop < 2.5f * ANIMATION_FRAME_DURATION &&
                        animationStateTimeInLoop > 1.5f * ANIMATION_FRAME_DURATION;
                if (attackPlayer) {
                    GameScreen.player.damagePlayer(damage);
                }
            }
        }
    }

    public void attack() {
//        SoundManager.hitSound.play();
        enemyState = EnemyState.ATTACK;
    }

    public void hit(float damage) {
        soundManager.playSound("hurt");
        healthPoints = Math.max(0, healthPoints - damage);
    }

    public void enableAttackToPlayer() {
        isAttackToPlayerEnabled = true;
        timeSinceLastMeleeAttack = ANIMATION_FRAME_DURATION;
    }

    public void disableAttackToPlayer() {
        isAttackToPlayerEnabled = false;
        timeSinceLastMeleeAttack = ANIMATION_FRAME_DURATION;
    }

    public static void createEnemies(GameApp game, World world, MapObjects objects, SoundManager soundManager) {
        for (MapObject object : objects) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            int enemyType = Integer.parseInt(object.getName()) - 1;
            float enemyRange = (enemyType == 3) ? 320 / Constants.PPM : 32 / Constants.PPM;
            Enemy.enemiesArray.add(new Enemy(game, world, enemyType, (int) rectangle.x,
                    (int) rectangle.y, enemyRange, soundManager));
        }
    }
}
