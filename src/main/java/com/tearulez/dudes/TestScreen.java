package com.tearulez.dudes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.tearulez.dudes.graphics.MenuScreen;
import com.tearulez.dudes.graphics.ViewportFactory;
import com.tearulez.dudes.graphics.WorldRenderer;

import java.util.concurrent.atomic.AtomicInteger;

public class TestScreen {
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Dudes";
        config.width = 800;
        config.height = 480;
        AtomicInteger count = new AtomicInteger();
        ViewportFactory viewportFactory = new ViewportFactory(config.width / config.height);

        Game game = new Game() {
            @Override
            public void create() {
                setScreen(new MenuScreen(
                        viewportFactory,
                        () -> System.out.println("resume " + count.getAndIncrement()),
                        () -> System.out.println("exit " + count.getAndIncrement()),
                        new WorldRenderer(viewportFactory, StateSnapshot::empty)));
            }
        };
        new LwjglApplication(game, config);
    }
}