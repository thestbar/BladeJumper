package com.thestbar.ludumdare54.listeners;

import com.badlogic.gdx.physics.box2d.*;
import com.thestbar.ludumdare54.gameobjects.Fireball;
import com.thestbar.ludumdare54.gameobjects.Powerup;
import com.thestbar.ludumdare54.screens.GameScreen;

public class ListenerClass implements ContactListener {
    private boolean playerOnGround = true;
    private boolean availableDoubleJump = true;
    private int enemyIdBeingHit = -1;
    @Override
    public void beginContact(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        System.out.println("fa: " + fa.getUserData() + ", fb: " + fb.getUserData());
        if (fa.getUserData() != null && fb.getUserData() != null && (
           (fa.getUserData().equals("lava") && fb.getUserData().equals("player")) ||
           (fa.getUserData().equals("player") && fb.getUserData().equals("lava")))) {
            if (GameScreen.player != null) {
                GameScreen.player.die();
            }
        }
        if ((fa.getUserData() != null && fa.getUserData().equals("ground_sensor")) ||
            (fb.getUserData() != null && fb.getUserData().equals("ground_sensor"))) {
            playerOnGround = true;
            availableDoubleJump = true;
        }
        if (fa.getUserData() != null && fb.getUserData() != null && (
           (fa.getUserData().equals("player") && ((String) fb.getUserData()).contains("powerup")) ||
           (fb.getUserData().equals("player") && ((String) fa.getUserData()).contains("powerup")))) {
            String powerupId;
            if (fa.getUserData().equals("player")) {
                powerupId = (String) fb.getUserData();
            } else {
                powerupId = (String) fa.getUserData();
            }
            Powerup powerup = Powerup.powerupMap.get(powerupId);
            GameScreen.bodiesToBeDeleted.add(powerup.body);
            Powerup.powerupsArray.removeValue(powerup, true);
        }
        if (fa.getUserData() != null && fb.getUserData() != null && (
           (fa.getUserData().equals("player") && ((String) fb.getUserData()).contains("enemy_sensor")) ||
           (fa.getUserData().equals("enemy_sensor") && ((String) fb.getUserData()).contains("player")))) {
            if (fa.getUserData().equals("player")) {
                enemyIdBeingHit = Integer.parseInt(((String) fb.getUserData()).substring(12));
            } else {
                enemyIdBeingHit = Integer.parseInt(((String) fa.getUserData()).substring(12));
            }
        }
        if (fa.getUserData() != null && fb.getUserData() != null && (
           (fa.getUserData().equals("ground") && ((String) fb.getUserData()).contains("fireball")) ||
           (((String) fa.getUserData()).contains("fireball") && fb.getUserData().equals("ground")))) {
            String fireballId;
            if (fa.getUserData().equals("ground")) {
                fireballId = fb.getUserData().toString();
            } else {
                fireballId = fa.getUserData().toString();
            }
            Fireball fireball = Fireball.fireballMap.get(fireballId);
            GameScreen.bodiesToBeDeleted.add(fireball.body);
            Fireball.activeFireballs.removeValue(fireball, true);
        }
        if (fa.getUserData() != null && fb.getUserData() != null && (
           (fa.getUserData().equals("level_end") && fb.getUserData().equals("player")) ||
           (fa.getUserData().equals("player") && fb.getUserData().equals("level_end")))) {
            if (GameScreen.player != null) {
                GameScreen.player.win();
            }
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
        if (fa.getUserData() != null && fb.getUserData() != null && (
           (fa.getUserData().equals("player") && ((String) fb.getUserData()).contains("enemy_sensor")) ||
           (fa.getUserData().equals("enemy_sensor") && ((String) fb.getUserData()).contains("player")))) {
            enemyIdBeingHit = -1;
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

    public int getEnemyIdBeingHit() {
        return enemyIdBeingHit;
    }
}
