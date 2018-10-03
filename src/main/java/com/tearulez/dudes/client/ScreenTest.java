package com.tearulez.dudes.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.tearulez.dudes.client.screens.SpawnScreen;
import com.tearulez.dudes.client.screens.ViewportFactory;
import com.tearulez.dudes.client.screens.WorldPresentation;
import com.tearulez.dudes.common.snapshot.StateSnapshot;
import com.tearulez.dudes.common.snapshot.Wall;
import com.tearulez.dudes.server.SvgMap;

import java.io.File;
import java.util.List;

public class ScreenTest {
    public static void main(String[] args) throws Exception {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Dudes");
        config.setWindowSizeLimits(800, -1, 480, -1);
        ViewportFactory viewportFactory = new ViewportFactory(800.0f / 480);
        List<Wall> walls = new SvgMap(new File("maps/map.svg")).getWalls();
        StateSnapshot snapshot = new StateSnapshot.Builder().setWalls(walls).build();
        Game game = new Game() {
            @Override
            public void create() {
                setScreen(new SpawnScreen(
                        viewportFactory,
                        new WorldPresentation(viewportFactory, () -> snapshot, new SoundSettings(0)),
                        () -> {
                        },
                        point -> {
                        },
                        "test"
                ));
            }
        };
        new Lwjgl3Application(game, config);
    }
}