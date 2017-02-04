package com.tearulez.dudes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.tearulez.dudes.graphics.*;

import java.util.*;

public class DudesGame extends Game {
    private static final int SCALE_FACTOR = 10;
    private static final String DUDES_SOUND_VOLUME = "DUDES_SOUND_VOLUME";
    private final GameClient gameClient;
    private List<Runnable> delayedActions = new ArrayList<>();
    private StateSnapshot stateSnapshot = StateSnapshot.empty();
    private WorldRenderer worldRenderer = null;
    private Sound shotSound = null;

    DudesGame(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    private synchronized void addDelayedAction(Runnable action) {
        delayedActions.add(action);
    }

    void onGameStateUpdate(StateSnapshot stateSnapshot) {
        addDelayedAction(() -> this.stateSnapshot = stateSnapshot);
    }

    void onPlayerRespawn(int playerId) {
        addDelayedAction(() -> {
            GameState gameState = () -> stateSnapshot;
            worldRenderer = new WorldRenderer(playerId, gameState, SCALE_FACTOR);
            setScreen(new GameScreen(gameClient, worldRenderer));
        });
    }

    void onPlayerDeath() {
        addDelayedAction(() -> setScreen(
                new DeathScreen(() -> setScreen(respawnScreen()))
        ));
    }

    void onShot() {
        addDelayedAction(() -> shotSound.play(Float.valueOf(System.getenv().get(DUDES_SOUND_VOLUME))));
    }

    private RespawnScreen respawnScreen() {
        return new RespawnScreen(worldRenderer, this::respawn);
    }

    private void respawn(Point point) {
        gameClient.respawnAt(point);
        setScreen(new LoadingScreen("Respawning"));
    }

    @Override
    public void create() {
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
}
