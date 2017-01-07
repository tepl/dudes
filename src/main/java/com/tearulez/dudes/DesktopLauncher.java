package com.tearulez.dudes;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        String host = args[0];
        Integer port = Integer.valueOf(args[1]);

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Dudes";
        config.width = 800;
        config.height = 480;
        GameClient gameClient = new GameClient(host, port);
        DudesGame game = new DudesGame(gameClient);
        new LwjglApplication(game, config);
    }
}