package com.tearulez.dudes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.tearulez.dudes.screens.*;
import org.lwjgl.input.Mouse;

import java.util.*;

public class DudesGame extends Game {
    private static final String DUDES_SOUND_VOLUME = "DUDES_SOUND_VOLUME";
    private final GameClient gameClient;
    private List<Runnable> delayedActions = new ArrayList<>();
    private StateSnapshot stateSnapshot = StateSnapshot.empty();
    private WorldPresentation worldPresentation = null;
    private ViewportFactory viewportFactory;
    private final SoundSettings soundSettings = new SoundSettings(
            Float.valueOf(System.getenv().get(DUDES_SOUND_VOLUME))
    );

    DudesGame(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    private synchronized void addDelayedAction(Runnable action) {
        delayedActions.add(action);
    }

    void onGameStateUpdate(StateSnapshot stateSnapshot) {
        addDelayedAction(() -> this.stateSnapshot = stateSnapshot);
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
                new MenuItem("Respawn", () -> setScreen(createRespawnScreen())),
                new MenuItem("Exit", this::exit)
        );
        return new MenuScreen(viewportFactory, menuItems, worldPresentation);
    }

    private RespawnScreen createRespawnScreen() {
        return new RespawnScreen(viewportFactory, worldPresentation, this::respawn);
    }

    void onPlayerDeath() {
        addDelayedAction(() -> setScreen(createDeathScreen()));
    }

    void onPlayerRespawn() {
        addDelayedAction(() -> setScreen(createGameScreen()));
    }

    private void respawn(Point point) {
        gameClient.respawnAt(point);
        setScreen(new LoadingScreen("Respawning"));
    }

    @Override
    public void create() {
        Gdx.input.setCursorCatched(true);
        Mouse.setClipMouseCoordinatesToWindow(true);
        // We need all our Viewports to have the same aspect ratio.
        // see Viewport.apply and HdpiUtils.glViewport
        viewportFactory = new ViewportFactory((float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight());
        worldPresentation = new WorldPresentation(viewportFactory, () -> stateSnapshot, soundSettings);
        gameClient.init(this);
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
}
