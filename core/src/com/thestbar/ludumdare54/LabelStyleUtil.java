package com.thestbar.ludumdare54;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import java.util.HashMap;
import java.util.Map;

public class LabelStyleUtil {
    private static Map<String, Label.LabelStyle> cache = new HashMap<>();

    private LabelStyleUtil() {}

    public static Label.LabelStyle getLabelStyle(GameApp game, String styleName, Color color) {
        String key = styleName + "_" + color.toString();
        if (!cache.containsKey(key)) {
            cache.put(key, new Label.LabelStyle(game.skin.getFont(styleName), color));
        }
        return cache.get(key);
    }
}
