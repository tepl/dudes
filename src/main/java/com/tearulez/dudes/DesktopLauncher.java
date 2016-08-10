package com.tearulez.dudes;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Drop";
        config.width = 800;
        config.height = 480;

        GameClient gameClient = new GameClient();
        gameClient.init(args[0], Integer.valueOf(args[1]));

        new LwjglApplication(new GameAdapter(gameClient), config);
    }
}