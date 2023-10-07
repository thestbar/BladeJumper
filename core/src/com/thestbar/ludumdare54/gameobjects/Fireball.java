package com.thestbar.ludumdare54.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.thestbar.ludumdare54.GameApp;
import com.thestbar.ludumdare54.utils.Box2DUtils;
import com.thestbar.ludumdare54.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class Fireball {
    public Body body;
    public static DelayedRemovalArray<Fireball> activeFireballs = new DelayedRemovalArray<>();
    public static Map<String, Fireball> fireballMap = new HashMap<>();
    public static int fireballCounter = 0;
    private final Animation<TextureRegion> animation;
    private float stateTime;
    private final float ANIMATION_FRAME_DURATION = 0.2f;
    private final int dirX;
    public float damage;
    public Fireball(GameApp game, World world, int x, int y, boolean isRight,
                    float damage, TextureRegion[] textureRegions) {
        activeFireballs.add(this);
        String id = "fireball" + fireballCounter++;
        fireballMap.put(id, this);

        // Create fireball
        body = Box2DUtils.createBox(world, x, y, 4, 4, BodyDef.BodyType.DynamicBody);
        body.getFixtureList().get(0).setDensity(1);
        body.getFixtureList().get(0).setFriction(0);
        body.getFixtureList().get(0).getFilterData().categoryBits = Constants.BIT_BULLET;
        body.getFixtureList().get(0).getFilterData().maskBits = Constants.BIT_PLAYER | Constants.BIT_GROUND | Constants.BIT_GROUND_SENSOR;
        body.getFixtureList().get(0).setSensor(true);
        body.getFixtureList().get(0).setUserData(id);

        dirX = (isRight) ? 1 : -1;
        body.applyForceToCenter(dirX * 10f, -Constants.GRAVITATIONAL_CONSTANT.y, true);

        stateTime = 0;

        animation = new Animation<>(ANIMATION_FRAME_DURATION, textureRegions);
        this.damage = damage;
    }

    public void render(SpriteBatch batch, boolean pauseAnimation) {
        if (!pauseAnimation) {
            stateTime += Gdx.graphics.getDeltaTime();
        }
        body.applyForceToCenter(dirX * 10f, -Constants.GRAVITATIONAL_CONSTANT.y, true);
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        float x = body.getPosition().x * Constants.PPM - 8;
        float y = body.getPosition().y * Constants.PPM - 8;
        batch.begin();
        batch.draw(currentFrame, x, y);
        batch.end();
    }
}
