package com.tearulez.dudes;

import com.badlogic.gdx.Game;

import java.util.*;

public class DudesGame extends Game {
    private final GameClient gameClient;
    private List<Runnable> delayedActions = new ArrayList<>();
    private GameScreen gameScreen;
    private StateSnapshot stateSnapshot = StateSnapshot.empty();

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
            gameScreen = new GameScreen(playerId, gameClient, gameState);
            setScreen(gameScreen);
        });
    }

    void onPlayerDeath() {
        addDelayedAction(() -> setScreen(new RespawnScreen(this::respawn)));
    }

    private void respawn() {
        gameClient.respawn();
        setScreen(new LoadingScreen("Respawning"));
    }

    @Override
    public void create() {
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

}
