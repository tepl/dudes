package com.tearulez.dudes.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.codahale.metrics.Timer;
import com.tearulez.dudes.client.screens.*;
import com.tearulez.dudes.common.snapshot.StateSnapshot;

import java.util.Arrays;
import java.util.List;

import static com.codahale.metrics.MetricRegistry.name;

public class DudesGame extends Game {
    private final GameClient gameClient;
    private StateSnapshot stateSnapshot = StateSnapshot.empty();
    private WorldPresentation worldPresentation = null;
    private ViewportFactory viewportFactory;
    private final SoundSettings soundSettings;
    private final Timer renderTime = Metrics.metrics.timer(name(DudesGame.class, "renderTime"));

    DudesGame(GameClient gameClient, float volume) {
        this.gameClient = gameClient;
        soundSettings = new SoundSettings(volume);
    }

    void onGameStateUpdate(StateSnapshot stateSnapshot) {
        Gdx.app.postRunnable(() -> this.stateSnapshot = stateSnapshot);
    }

    private GameScreen createGameScreen() {
        return new GameScreen(viewportFactory, gameClient, () -> setScreen(createEscapeScreen()), worldPresentation);
    }

    private MenuScreen createEscapeScreen() {
        List<MenuItem> menuItems = Arrays.asList(
                new MenuItem("Resume", () -> setScreen(createGameScreen())),
                new MenuItem("Exit", this::exit)
        );
        return new MenuScreen(viewportFactory, menuItems, worldPresentation);
    }

    private MenuScreen createDeathScreen() {
        List<MenuItem> menuItems = Arrays.asList(
                new MenuItem("Respawn", () -> setScreen(createSpawnScreen("Choose a spawn point"))),
                new MenuItem("Exit", this::exit)
        );
        return new MenuScreen(viewportFactory, menuItems, worldPresentation);
    }

    private SpawnScreen createSpawnScreen(String s) {
        return new SpawnScreen(
                viewportFactory,
                worldPresentation,
                () -> setScreen(createDeathScreen()),
                gameClient::spawnAt,
                s
        );
    }

    void onPlayerDeath() {
        Gdx.app.postRunnable(() -> setScreen(createDeathScreen()));
    }

    void onPlayerSpawn(boolean success) {
        if (success) {
            Gdx.app.postRunnable(() -> setScreen(createGameScreen()));
        } else {
            Gdx.app.postRunnable(() -> setScreen(createSpawnScreen("Cannot spawn here, because you are in the line of sight of another player")));
        }
    }

    @Override
    public void create() {
        Gdx.input.setCursorCatched(true);
        // We need all our Viewports to have the same aspect ratio.
        // see Viewport.apply and HdpiUtils.glViewport
        viewportFactory = new ViewportFactory((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight());
        worldPresentation = new WorldPresentation(viewportFactory, () -> stateSnapshot, soundSettings);
        gameClient.init(this);
        setScreen(createSpawnScreen("Choose a spawn point"));
    }

    @Override
    public void setScreen(Screen screen) {
        Screen previousScreen = getScreen();
        super.setScreen(screen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        getScreen().dispose();
        worldPresentation.dispose();
    }

    private void exit() {
        Gdx.app.exit();
    }

    @Override
    public void render() {
        Timer.Context context = renderTime.time();
        super.render();
        context.stop();
    }
}
