package com.thestbar.ludumdare54;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.thestbar.ludumdare54.GameApp;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	private static final int SCREEN_SIZE_MULTIPLIER = 2;
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("LudumDare54");
		config.setWindowedMode(720 * SCREEN_SIZE_MULTIPLIER, 468 * SCREEN_SIZE_MULTIPLIER);
		config.setResizable(false);
		new Lwjgl3Application(new GameApp(), config);
	}
}
