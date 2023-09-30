package com.thestbar.ludumdare54;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    public BodyDef bodyDef;
    public Body body;

    public Player() {
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(5, 10);


        CircleShape circle = new CircleShape();

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // This will make the player to bounce

        Fixture fixture = body.createFixture(fixtureDef);

        circle.dispose();
    }
}
