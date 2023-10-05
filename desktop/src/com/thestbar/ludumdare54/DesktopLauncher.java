package com.thestbar.ludumdare54;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.thestbar.ludumdare54.utils.Constants;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("Blade Jumper");
//		config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
		config.setWindowedMode(1080 * Constants.SCREEN_SIZE_MULTIPLIER, 700 * Constants.SCREEN_SIZE_MULTIPLIER);
		config.setResizable(false);
		new Lwjgl3Application(new GameApp(), config);
	}
}
