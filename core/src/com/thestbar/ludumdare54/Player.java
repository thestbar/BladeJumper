package com.thestbar.ludumdare54;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.thestbar.ludumdare54.utils.Box2DUtils;
import com.thestbar.ludumdare54.utils.Constants;

import static com.thestbar.ludumdare54.utils.Constants.PPM;

public class Player {
    public Body body;
    private Texture texture;

    public Player(World world) {
        // Create player
        body = Box2DUtils.createBox(world,140, 820, 10, 14, BodyDef.BodyType.DynamicBody);
        body.getFixtureList().get(0).setDensity(1);
        body.getFixtureList().get(0).setFriction(0);
        body.getFixtureList().get(0).getFilterData().categoryBits = Constants.BIT_PLAYER;
        body.getFixtureList().get(0).getFilterData().maskBits = Constants.BIT_GROUND;
        body.getFixtureList().get(0).setUserData("player");

        texture = new Texture(Gdx.files.internal("spritesheets/ld54-player.png"));

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
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, body.getPosition().x * PPM - texture.getWidth() / 2f,
                body.getPosition().y * PPM - texture.getHeight() / 2f);
    }

    public void jump(int force) {
        body.applyForceToCenter(0, force, true);
    }

    public void move(int horizontalForce) {
        body.setLinearVelocity(horizontalForce * 7, body.getLinearVelocity().y);
    }

    public void dispose() {
        texture.dispose();
    }
}
