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
import com.thestbar.ludumdare54.managers.SoundManager;
import com.thestbar.ludumdare54.screens.GameScreen;
import com.thestbar.ludumdare54.utils.Box2DUtils;
import com.thestbar.ludumdare54.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class Enemy {
    public Body body;
    public int enemyType;
    private Animation<TextureRegion> restAnimation;
    private Animation<TextureRegion> attackAnimation;
    private Animation<TextureRegion> flippedRestAnimation;
    private Animation<TextureRegion> flippedAttackAnimation;
    private float stateTime;
    public EnemyState enemyState;
    public static int enemies = 0;
    private final float ANIMATION_FRAME_DURATION = 0.2f;
    public float range;
    public static Map<String, Enemy> enemiesMap = new HashMap<>();
    public static Array<Enemy> enemiesArray = new Array<>();
    public float healthPoints;
    private float maxHealthPoints;
    private TextureRegion[] healthBarTextures;
    private Texture enemyBulletSpritesheet;
    private float timeSinceLastRangeAttack;
    private World world;

    public Enemy(World world, int enemyType, Texture texture, int x, int y, float range,
                 Texture healthBarSpritesheet, Texture enemyBulletSpritesheet) {
        this.enemyBulletSpritesheet = enemyBulletSpritesheet;
        this.enemyType = enemyType;
        this.range = range;
        this.world = world;
        switch (enemyType) {
            case 0:
                healthPoints = 200;
                break;
            case 1:
                healthPoints = 150;
                break;
            case 2:
                healthPoints = 80;
                break;
            case 3:
                healthPoints = 60;
                break;
            default:
                healthPoints = 0;
                break;
        }
        maxHealthPoints = healthPoints;

        String id = "enemy" + (++enemies);
        enemiesMap.put(id, this);

        body = Box2DUtils.createBox(world,x, y, 10, 14, BodyDef.BodyType.DynamicBody);
        body.getFixtureList().get(0).setDensity(1);
        body.getFixtureList().get(0).setFriction(0);
        body.getFixtureList().get(0).getFilterData().categoryBits = Constants.BIT_ENEMY;
        body.getFixtureList().get(0).getFilterData().maskBits = Constants.BIT_GROUND | Constants.BIT_PLAYER;
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

        TextureRegion[][] tmp = TextureRegion.split(texture, 16, 16);

        TextureRegion[][] tmpFlipped = TextureRegion.split(texture, 16, 16);

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
        healthBarTextures = TextureRegion.split(healthBarSpritesheet, 16, 16)[0];

        timeSinceLastRangeAttack = 0;
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
                new Fireball(world, enemyBulletSpritesheet, (int) (body.getPosition().x * Constants.PPM),
                        (int) (body.getPosition().y * Constants.PPM), flip);
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
    }

    public void attack() {
        enemyState = EnemyState.ATTACK;
    }

    public void hit(float damage) {
        SoundManager.hurtSound.play();
        healthPoints = Math.max(0, healthPoints - damage);
    }

    public static void createEnemies(World world, MapObjects objects, Texture enemiesSpritesheet,
                                     Texture healthBarSpritesheet, Texture enemyBulletSpritesheet) {
        for (MapObject object : objects) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            int enemyType = Integer.parseInt(object.getName()) - 1;
            float enemyRange = (enemyType == 3) ? 320 / Constants.PPM : 32 / Constants.PPM;
            Enemy.enemiesArray.add(new Enemy(world, enemyType, enemiesSpritesheet, (int) rectangle.x,
                    (int) rectangle.y, enemyRange, healthBarSpritesheet, enemyBulletSpritesheet));
        }
    }
}
