package com.thestbar.ludumdare54.utils;

import com.badlogic.gdx.physics.box2d.*;

public final class Box2DUtils {
    public static Body createBox(World world, int x, int y, int width, int height, BodyDef.BodyType bodyType) {
        Body body;
        BodyDef def = new BodyDef();
        def.type = bodyType;
        def.position.set(x / Constants.PPM, y / Constants.PPM);
        def.fixedRotation = true;
        def.linearDamping = 2f;
        body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f / Constants.PPM, height / 2f / Constants.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }
}
