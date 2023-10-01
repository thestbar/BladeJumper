package com.thestbar.ludumdare54.listeners;

import com.badlogic.gdx.physics.box2d.*;

public class ListenerClass implements ContactListener {
    private boolean playerOnGround = true;
    private boolean availableDoubleJump = true;
    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if ((fa.getUserData() != null && fa.getUserData().equals("ground_sensor")) ||
            (fb.getUserData() != null && fb.getUserData().equals("ground_sensor"))) {
            playerOnGround = true;
            availableDoubleJump = true;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if ((fa.getUserData() != null && fa.getUserData().equals("ground_sensor")) ||
                (fb.getUserData() != null && fb.getUserData().equals("ground_sensor"))) {
            playerOnGround = false;
            availableDoubleJump = true;
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public boolean isPlayerOnGround() {
        return playerOnGround;
    }

    public boolean isAvailableDoubleJump() {
        return availableDoubleJump;
    }

    public void useDoubleJump() {
        availableDoubleJump = false;
    }
}
