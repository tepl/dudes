package com.tearulez.dudes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.tearulez.dudes.graphics.*;
import org.lwjgl.input.Mouse;

import java.util.*;

public class DudesGame extends Game {
    private static final String DUDES_SOUND_VOLUME = "DUDES_SOUND_VOLUME";
    private final GameClient gameClient;
    private List<Runnable> delayedActions = new ArrayList<>();
    private StateSnapshot stateSnapshot = StateSnapshot.empty();
    private WorldRenderer worldRenderer = null;
    private Sound shotSound = null;
    private GameScreen gameScreen = null;
    private MenuScreen menuScreen = null;
    private ViewportFactory viewportFactory;

    DudesGame(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    private synchronized void addDelayedAction(Runnable action) {
        delayedActions.add(action);
    }

    void onGameStateUpdate(StateSnapshot stateSnapshot) {
        addDelayedAction(() -> this.stateSnapshot = stateSnapshot);
    }

    void onPlayerRespawn() {
        addDelayedAction(() -> setScreen(getGameScreen()));
    }

    private GameScreen getGameScreen() {
        if (gameScreen == null) {
            gameScreen = new GameScreen(viewportFactory, gameClient, () -> setScreen(createMenuScreen()), worldRenderer);
        }
        return gameScreen;
    }

    private MenuScreen createMenuScreen() {
        if (menuScreen == null) {
            List<MenuItem> menuItems = Arrays.asList(
                    new MenuItem("Resume", () -> setScreen(getGameScreen())),
                    new MenuItem("Exit", this::exit)
            );
            menuScreen = new MenuScreen(viewportFactory, menuItems, worldRenderer);
        }
        return menuScreen;
    }

    void onPlayerDeath() {
        List<MenuItem> menuItems = Arrays.asList(
                new MenuItem("Respawn", () -> setScreen(createRespawnScreen())),
                new MenuItem("Exit", this::exit)
        );
        addDelayedAction(() -> setScreen(new MenuScreen(viewportFactory, menuItems, worldRenderer)));
    }

    void onShot() {
        addDelayedAction(() -> shotSound.play(Float.valueOf(System.getenv().get(DUDES_SOUND_VOLUME))));
    }

    private RespawnScreen createRespawnScreen() {
        return new RespawnScreen(viewportFactory, worldRenderer, this::respawn);
    }

    private void respawn(Point point) {
        gameClient.respawnAt(point);
        setScreen(new LoadingScreen("Respawning"));
    }

    @Override
    public void create() {
        Gdx.input.setCursorCatched(true);
        Mouse.setClipMouseCoordinatesToWindow(true);
        viewportFactory = new ViewportFactory((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight());
        worldRenderer = new WorldRenderer(viewportFactory, () -> stateSnapshot);
        gameClient.init(this);
        shotSound = Gdx.audio.newSound(Gdx.files.internal("sounds/M4A1.mp3"));
        setScreen(new LoadingScreen("Connecting"));
    }

    @Override
    public synchronized void render() {
        for (Runnable action : delayedActions) {
            action.run();
        }
        delayedActions.clear();
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        shotSound.dispose();
    }

    private void exit() {
        Gdx.app.exit();
    }
}
