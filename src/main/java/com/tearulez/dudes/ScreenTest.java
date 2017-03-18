package com.tearulez.dudes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.tearulez.dudes.screens.MenuItem;
import com.tearulez.dudes.screens.MenuScreen;
import com.tearulez.dudes.screens.ViewportFactory;
import com.tearulez.dudes.screens.WorldPresentation;

import java.util.Collections;

public class ScreenTest {
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Dudes";
        config.width = 800;
        config.height = 480;
        ViewportFactory viewportFactory = new ViewportFactory(config.width / config.height);

        Game game = new Game() {
            @Override
            public void create() {
                setScreen(new MenuScreen(
                        viewportFactory,
                        Collections.singletonList(new MenuItem("Test", () -> System.out.println("Test"))),
                        new WorldPresentation(viewportFactory, StateSnapshot::empty, new SoundSettings(0))));
            }
        };
        new LwjglApplication(game, config);
    }
}