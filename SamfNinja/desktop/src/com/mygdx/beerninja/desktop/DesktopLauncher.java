package com.mygdx.beerninja.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.beerninja.SamfNinja;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "SamfNinja";
		config.height = 1050;
		config.width = 540;
		new LwjglApplication(new SamfNinja(), config);

	}
}
