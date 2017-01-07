package com.tearulez.dudes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Dudes";
        config.width = 800;
        config.height = 480;

        GameClient gameClient = new GameClient();
        gameClient.init(args[0], Integer.valueOf(args[1]));

        Game game = new Game() {
            @Override
            public void create() {
                GameScreen gameScreen = new GameScreen(gameClient);
                MainMenuScreen mainMenuScreen = new MainMenuScreen(this, gameScreen);
                setScreen(mainMenuScreen);
            }
        };
        new LwjglApplication(game, config);
    }
}