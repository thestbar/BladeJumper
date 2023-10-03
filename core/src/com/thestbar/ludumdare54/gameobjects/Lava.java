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
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.thestbar.ludumdare54.utils.Box2DUtils;
import com.thestbar.ludumdare54.utils.Constants;

public class Lava {
    public Body body;
    private Animation<TextureRegion> animation;
    private float stateTime;
    private final float ANIMATION_FRAME_DURATION = 0.2f;
    public static Array<Lava> lavaArray = new Array<>();

    public Lava(World world, Texture texture, int x, int y) {
        lavaArray.add(this);
//        System.out.println(x + ", " + y);
        body = Box2DUtils.createBox(world, x, y, 16, 10, BodyDef.BodyType.StaticBody);
        body.getFixtureList().get(0).setDensity(1);
        body.getFixtureList().get(0).setFriction(0);
        body.getFixtureList().get(0).getFilterData().categoryBits = Constants.BIT_LAVA;
        body.getFixtureList().get(0).getFilterData().maskBits = Constants.BIT_PLAYER;
        body.getFixtureList().get(0).setUserData("lava");
        TextureRegion[] tmp = TextureRegion.split(texture, 16, 16)[0];
        animation = new Animation<>(ANIMATION_FRAME_DURATION, tmp);
    }

    public void render(SpriteBatch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        float x = body.getPosition().x * Constants.PPM - 8;
        float y = body.getPosition().y * Constants.PPM - 8;
        batch.begin();
        batch.draw(currentFrame, x, y);
        batch.end();
    }

    public static void createLavas(World world, MapObjects objects,
                                   Texture lavasSpritesheet) {
        for (MapObject object : objects) {
//            System.out.println(object.getClass());
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            new Lava(world, lavasSpritesheet,
                    (int) (rectangle.x + 8), (int) (rectangle.y + 8));
        }
    }
}
