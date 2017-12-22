package com.tearulez.dudes.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
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
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Dudes";
        config.width = 800;
        config.height = 480;
        ViewportFactory viewportFactory = new ViewportFactory(config.width / config.height);
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
        new LwjglApplication(game, config);
    }
}