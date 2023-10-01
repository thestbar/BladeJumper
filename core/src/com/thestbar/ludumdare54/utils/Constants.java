package com.thestbar.ludumdare54.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public final class Constants {
    // Pixels per meter
    // To go from box2D to pixels we multiply with PPM
    // To go from screen coordinates to world coordinates we divide by PPM
    public static final float PPM = 16;

    public static final float SCALE = 4f;

    public static final Vector2 GRAVITATIONAL_CONSTANT = new Vector2(0, -30f);

    public static final int SCREEN_SIZE_MULTIPLIER = 2;

    public static final Color BACKGROUND_COLOR = new Color(0.03922f, 0.15686f, 0.18824f, 1);

    public static final Color DEBUG_BACKGROUND_COLOR = Color.GRAY;

    public static final short BIT_GROUND = 2;

    public static final short BIT_PLAYER = 4;

    public static final short BIT_GROUND_SENSOR = 8;
}
