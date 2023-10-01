package com.thestbar.ludumdare54;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.thestbar.ludumdare54.utils.Box2DUtils;
import com.thestbar.ludumdare54.utils.Constants;

import static com.thestbar.ludumdare54.utils.Constants.PPM;

public class Player {
    public Body body;
    private Texture texture;
    private Animation<TextureRegion> restAnimation;
    private Animation<TextureRegion> moveLeftAnimation;
    private Animation<TextureRegion> moveRightAnimation;
    private Animation<TextureRegion> dieAnimation;
    private Animation<TextureRegion> weaponLeftAnimation;
    private Animation<TextureRegion> weaponRightAnimation;
    private Animation<TextureRegion> weaponAttackLeftAnimation;
    private Animation<TextureRegion> weaponAttackRightAnimation;
    private float stateTime;
    public PlayerState playerState = PlayerState.REST;
    private boolean isAttacking;
    private float weaponStateTime;
    private final float WEAPON_FRAME_DURATION = 0.1f;

    public Player(World world) {
        // Create player
        body = Box2DUtils.createBox(world,140, 820, 10, 14, BodyDef.BodyType.DynamicBody);
        body.getFixtureList().get(0).setDensity(1);
        body.getFixtureList().get(0).setFriction(0);
        body.getFixtureList().get(0).getFilterData().categoryBits = Constants.BIT_PLAYER;
        body.getFixtureList().get(0).getFilterData().maskBits = Constants.BIT_GROUND;
        body.getFixtureList().get(0).setUserData("player");

        // Create player ground sensor
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(4 / PPM, 1 / PPM, new Vector2(0, -7 / PPM), 0);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Constants.BIT_PLAYER;
        fixtureDef.filter.maskBits = Constants.BIT_GROUND;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef).setUserData("ground_sensor");
        shape.dispose();

        texture = new Texture(Gdx.files.internal("spritesheets/ld54-player-Sheet.png"));

        TextureRegion[][] tmp = TextureRegion.split(texture, 16, 16);

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
    }

    public enum PlayerState {
        REST,
        MOVE_LEFT,
        MOVE_RIGHT,
        DIE
    }

    public void render(SpriteBatch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        weaponStateTime += Gdx.graphics.getDeltaTime();
        renderPlayer(batch);

        if (playerState != PlayerState.REST) {
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
        x += body.getPosition().x * PPM;
        float y = body.getPosition().y * PPM - 9f;
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
        float x = body.getPosition().x * PPM - 8f;
        float y = body.getPosition().y * PPM - 8f;
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
        batch.draw(currentPlayerAnimationKeyFrame, body.getPosition().x * PPM - 8f,
                body.getPosition().y * PPM - 8f);
        batch.end();
    }

    public void jump(int force) {
        body.applyForceToCenter(0, force, true);
    }

    public void move(int horizontalForce) {
        body.setLinearVelocity(horizontalForce * 7, body.getLinearVelocity().y);
    }

    public void attack() {
        isAttacking = true;
        weaponStateTime = 0f;
    }

    public void dispose() {
        texture.dispose();
    }
}
