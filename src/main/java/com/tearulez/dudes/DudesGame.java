package com.tearulez.dudes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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

    private GameScreen createGameScreen() {
        return new GameScreen(viewportFactory, gameClient, () -> setScreen(createEscapeScreen()), worldRenderer);
    }

    private MenuScreen createEscapeScreen() {
        List<MenuItem> menuItems = Arrays.asList(
                new MenuItem("Resume", () -> setScreen(createGameScreen())),
                new MenuItem("Exit", this::exit)
        );
        return new MenuScreen(viewportFactory, menuItems, worldRenderer);
    }

    private MenuScreen createDeathScreen() {
        List<MenuItem> menuItems = Arrays.asList(
                new MenuItem("Respawn", () -> setScreen(createRespawnScreen())),
                new MenuItem("Exit", this::exit)
        );
        return new MenuScreen(viewportFactory, menuItems, worldRenderer);
    }

    private RespawnScreen createRespawnScreen() {
        return new RespawnScreen(viewportFactory, worldRenderer, this::respawn);
    }

    void onShot() {
        addDelayedAction(() -> shotSound.play(Float.valueOf(System.getenv().get(DUDES_SOUND_VOLUME))));
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
        shotSound.dispose();
    }

    private void exit() {
        Gdx.app.exit();
    }
}
