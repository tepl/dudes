package com.tearulez.dudes;

import com.badlogic.gdx.Gdx;
import com.tearulez.dudes.networking.Client;
import com.tearulez.dudes.networking.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class GameClient implements PlayerControls {
    private static final Logger log = LoggerFactory.getLogger(GameClient.class);
    private final Client client;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Connection connection;

    GameClient(String serverHost, int serverPort) {
        client = new Client(serverHost, serverPort);
    }

    void init(DudesGame game) {
        try {
            connection = client.connect();
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
            throw new Error();
        }

        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Object> messages = connection.receive();
                for (Object message : messages) {
                    // Spawn response
                    if (message instanceof Network.SpawnResponse) {
                        Network.SpawnResponse spawnResponse = (Network.SpawnResponse) message;
                        log.info("onPlayerSpawn: " + spawnResponse.success);
                        game.onPlayerSpawn(spawnResponse.success);
                    }

                    // Update received
                    if (message instanceof Network.UpdateModel) {
                        Network.UpdateModel updateModel = (Network.UpdateModel) message;
                        log.trace("onGameStateUpdate");
                        game.onGameStateUpdate(updateModel.stateSnapshot);
                    }

                    // Player died
                    if (message instanceof Network.PlayerDeath) {
                        log.info("onPlayerDeath");
                        game.onPlayerDeath();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Gdx.app.exit();
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    public void movePlayer(float dx, float dy) {
        Network.MovePlayer movePlayer = new Network.MovePlayer();
        movePlayer.dx = dx;
        movePlayer.dy = dy;
        sendToServer(movePlayer);
    }

    public void shootAt(float x, float y) {
        Network.ShootAt shootAt = new Network.ShootAt();
        shootAt.x = x;
        shootAt.y = y;
        sendToServer(shootAt);
    }

    @Override
    public void reload() {
        sendToServer(new Network.Reload());
    }

    void spawnAt(Point point) {
        Network.SpawnRequest spawnRequest = new Network.SpawnRequest();
        spawnRequest.startingPosition = point;
        sendToServer(spawnRequest);
    }

    private void sendToServer(Serializable movePlayer) {
        try {
            connection.send(movePlayer);
        } catch (IOException e) {
            e.printStackTrace();
            Gdx.app.exit();
        }
    }
}
