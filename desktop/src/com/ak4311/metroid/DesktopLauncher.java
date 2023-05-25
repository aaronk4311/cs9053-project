package com.ak4311.metroid;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

//Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("Metroid");
		config.setWindowedMode(Metroid.WINDOW_WIDTH, Metroid.WINDOW_HEIGHT);
		new Lwjgl3Application(new Metroid(), config);
	}
}
