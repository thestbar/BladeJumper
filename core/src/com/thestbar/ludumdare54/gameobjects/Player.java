package com.thestbar.ludumdare54.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.thestbar.ludumdare54.GameApp;
import com.thestbar.ludumdare54.managers.SoundManager;
import com.thestbar.ludumdare54.utils.Box2DUtils;
import com.thestbar.ludumdare54.utils.Constants;

public class Player {
    public GameApp game;
    public Body body;
    private final Animation<TextureRegion> restAnimation;
    private final Animation<TextureRegion> moveLeftAnimation;
    private final Animation<TextureRegion> moveRightAnimation;
    private final Animation<TextureRegion> dieAnimation;
    private final Animation<TextureRegion> weaponLeftAnimation;
    private final Animation<TextureRegion> weaponRightAnimation;
    private final Animation<TextureRegion> weaponAttackLeftAnimation;
    private final Animation<TextureRegion> weaponAttackRightAnimation;
    private float stateTime;
    public PlayerState playerState = PlayerState.REST;
    private boolean isAttacking;
    private float weaponStateTime;
    private final float WEAPON_FRAME_DURATION = 0.1f;
    public float playerDamage;
    public float maxHealthPoints;
    public float healthPoints;
    private SoundManager soundManager;

    public Player(GameApp game, World world, int x, int y, SoundManager soundManager) {
        this.game = game;
        this.soundManager = soundManager;
        // Create player
        body = Box2DUtils.createBox(world, x, y, 10, 14, BodyDef.BodyType.DynamicBody);
        body.getFixtureList().get(0).setDensity(1);
        body.getFixtureList().get(0).setFriction(0);
        body.getFixtureList().get(0).getFilterData().categoryBits = Constants.BIT_PLAYER;
        body.getFixtureList().get(0).getFilterData().maskBits = Constants.BIT_GROUND |
                Constants.BIT_ENEMY_ATTACK_SENSOR | Constants.BIT_POWERUP | Constants.BIT_ENEMY_SENSOR |
                Constants.BIT_LAVA | Constants.BIT_BULLET | Constants.BIT_LEVEL_END | Constants.BIT_GROUND_SENSOR;
        body.getFixtureList().get(0).setUserData("player");

        // Create player ground sensor
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(4 / Constants.PPM, 1 / Constants.PPM, new Vector2(0, -7 / Constants.PPM), 0);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Constants.BIT_GROUND_SENSOR;
        fixtureDef.filter.maskBits = Constants.BIT_GROUND;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef).setUserData("ground_sensor");
        shape.dispose();

        TextureAtlas textureAtlas = game.assetManager.get("spritesheets/atlas/ld54.atlas", TextureAtlas.class);
        TextureRegion[][] tmp = new TextureRegion[8][4];
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 4; ++j) {
                int index = 4 * i + j + 1;
                String regionName = "ld54-player-Sheet" + index;
                tmp[i][j] = textureAtlas.findRegion(regionName);
            }
        }

        restAnimation = new Animation<>(0.2f, tmp[0]);
        moveRightAnimation = new Animation<>(0.2f, tmp[1]);
        moveLeftAnimation = new Animation<>(0.2f, tmp[2]);
        dieAnimation = new Animation<>(0.2f, tmp[3]);
        weaponRightAnimation = new Animation<>(0.2f, tmp[4]);
        weaponLeftAnimation = new Animation<>(0.2f, tmp[5]);
        weaponAttackRightAnimation = new Animation<>(WEAPON_FRAME_DURATION, tmp[6]);
        weaponAttackLeftAnimation = new Animation<>(WEAPON_FRAME_DURATION, tmp[7]);

        stateTime = 0f;
        isAttacking = false;
        weaponStateTime = 0f;
        playerDamage = 30f;
        healthPoints = 120f;
        maxHealthPoints = 120f;
    }

    public enum PlayerState {
        REST,
        MOVE_LEFT,
        MOVE_RIGHT,
        DIE,
        WIN
    }

    public void render(SpriteBatch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        weaponStateTime += Gdx.graphics.getDeltaTime();

        renderPlayer(batch);

        if (playerState != PlayerState.REST && playerState != PlayerState.DIE) {
            if (isAttacking) {
                renderWeaponAttack(batch);
            } else {
                renderWeaponRest(batch);
            }
        }

        // Check if attack finished
        if (isAttacking && weaponStateTime > 4 * WEAPON_FRAME_DURATION) {
            weaponStateTime = 0f;
            isAttacking = false;
        }
    }

    private void renderWeaponAttack(SpriteBatch batch) {
        Animation<TextureRegion> currentWeaponAnimation;
        if (playerState == PlayerState.MOVE_LEFT) {
            currentWeaponAnimation = weaponAttackLeftAnimation;
        } else {
            currentWeaponAnimation = weaponAttackRightAnimation;
        }
        TextureRegion currentWeaponAnimationKeyFrame = currentWeaponAnimation.getKeyFrame(stateTime, true);
        float x = (playerState == PlayerState.MOVE_LEFT) ? -19f : 3f;
        x += body.getPosition().x * Constants.PPM;
        float y = body.getPosition().y * Constants.PPM - 9f;
        batch.begin();
        batch.draw(currentWeaponAnimationKeyFrame, x, y);
        batch.end();
    }

    private void renderWeaponRest(SpriteBatch batch) {
        Animation<TextureRegion> currentWeaponAnimation;
        if (playerState == PlayerState.MOVE_LEFT) {
            currentWeaponAnimation = weaponLeftAnimation;
        } else {
            currentWeaponAnimation = weaponRightAnimation;
        }
        TextureRegion currentWeaponAnimationKeyFrame = currentWeaponAnimation.getKeyFrame(stateTime, true);
        float x = body.getPosition().x * Constants.PPM - 8f;
        float y = body.getPosition().y * Constants.PPM - 8f;
        batch.begin();
        batch.draw(currentWeaponAnimationKeyFrame, x, y);
        batch.end();
    }

    private void renderPlayer(SpriteBatch batch) {
        Animation<TextureRegion> currentPlayerAnimation;
        switch (playerState) {
            case MOVE_LEFT:
                currentPlayerAnimation = moveLeftAnimation;
                break;
            case MOVE_RIGHT:
                currentPlayerAnimation = moveRightAnimation;
                break;
            case DIE:
                currentPlayerAnimation = dieAnimation;
                break;
            default:
                currentPlayerAnimation = restAnimation;
                break;
        }

        batch.begin();
        TextureRegion currentPlayerAnimationKeyFrame = currentPlayerAnimation.getKeyFrame(stateTime, true);
        batch.draw(currentPlayerAnimationKeyFrame, body.getPosition().x * Constants.PPM - 8f,
                body.getPosition().y * Constants.PPM - 8f);
        batch.end();
    }

    public void jump(int force) {
        soundManager.playSound("jump");
        body.applyForceToCenter(0, force, true);
    }

    public void move(int horizontalForce) {
        body.setLinearVelocity(horizontalForce * 7, body.getLinearVelocity().y);
    }

    public void attack() {
        soundManager.playSound("hit");
        isAttacking = true;
        weaponStateTime = 0f;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void die() {
        soundManager.playSound("lose");
        playerState = PlayerState.DIE;
        healthPoints = 0f;
    }

    public void win() {
        soundManager.playSound("win");
        playerState = PlayerState.WIN;
    }

    public void addPowerUp(Powerup powerup) {
        soundManager.playSound("powerup");
    }

    public void damagePlayer(float damage) {
        if (playerState == PlayerState.DIE || playerState == PlayerState.WIN) {
            return;
        }
        soundManager.playSound("hurt");
        healthPoints = Math.max(0, healthPoints - damage);
        if (healthPoints == 0) {
            die();
        }
    }

    public void dispose() {}
}
