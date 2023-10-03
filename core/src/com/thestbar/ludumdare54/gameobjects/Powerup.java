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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.thestbar.ludumdare54.utils.Box2DUtils;
import com.thestbar.ludumdare54.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class Powerup {
    public Body body;
    public int powerupType;
    private Animation<TextureRegion> animation;
    private float stateTime;
    public static int powerups = 0;
    private final float ANIMATION_FRAME_DURATION = 0.2f;
    public static Map<String, Powerup> powerupMap = new HashMap<>();
    public static DelayedRemovalArray<Powerup> powerupsArray = new DelayedRemovalArray<>();;

    public Powerup(World world, Texture texture, int powerupType, int x, int y) {
        this.powerupType = powerupType;

        String id = "powerup" + (powerups++);
        powerupMap.put(id, this);

        body = Box2DUtils.createBox(world, x, y, 16, 16, BodyDef.BodyType.KinematicBody);
        body.getFixtureList().get(0).setDensity(1);
        body.getFixtureList().get(0).setFriction(0);
        body.getFixtureList().get(0).getFilterData().categoryBits = Constants.BIT_POWERUP;
        body.getFixtureList().get(0).getFilterData().maskBits = Constants.BIT_PLAYER;
        body.getFixtureList().get(0).setUserData(id);
        body.getFixtureList().get(0).setSensor(true);

        TextureRegion[][] tmp = TextureRegion.split(texture, 16, 16);

        animation = new Animation<>(ANIMATION_FRAME_DURATION, tmp[powerupType]);
    }

    public void render(SpriteBatch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        float x = body.getPosition().x * Constants.PPM - 8f;
        float y = body.getPosition().y * Constants.PPM - 8f;
        batch.begin();
        batch.draw(currentFrame, x, y);
        batch.end();
    }

    public void consume(Player player) {

    }

    public static void createPowerups(World world, MapObjects objects,
                                     Texture powerupsSpritesheet) {
        for (MapObject object : objects) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            int powerupType = Integer.parseInt(object.getName()) - 1;
            Powerup.powerupsArray.add(new Powerup(world, powerupsSpritesheet,
                    powerupType, (int) rectangle.x, (int) (rectangle.y + 8)));
        }
    }
}
