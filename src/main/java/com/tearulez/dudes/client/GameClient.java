package com.tearulez.dudes.client;

import com.badlogic.gdx.Gdx;
import com.tearulez.dudes.common.Messages;
import com.tearulez.dudes.common.networking.Client;
import com.tearulez.dudes.common.networking.Connection;
import com.tearulez.dudes.common.snapshot.Point;
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
                    if (message instanceof Messages.SpawnResponse) {
                        Messages.SpawnResponse spawnResponse = (Messages.SpawnResponse) message;
                        log.info("onPlayerSpawn: " + spawnResponse.success);
                        game.onPlayerSpawn(spawnResponse.success);
                    }

                    // Update received
                    if (message instanceof Messages.UpdateModel) {
                        Messages.UpdateModel updateModel = (Messages.UpdateModel) message;
                        log.trace("onGameStateUpdate");
                        game.onGameStateUpdate(updateModel.stateSnapshot);
                    }

                    // Player died
                    if (message instanceof Messages.PlayerDeath) {
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
        Messages.MovePlayer movePlayer = new Messages.MovePlayer();
        movePlayer.dx = dx;
        movePlayer.dy = dy;
        sendToServer(movePlayer);
    }

    public void rotatePlayer(float angle) {
        Messages.RotatePlayer rotatePlayer = new Messages.RotatePlayer();
        rotatePlayer.angle = angle;
        sendToServer(rotatePlayer);
    }

    public void shootAt(float x, float y) {
        Messages.ShootAt shootAt = new Messages.ShootAt();
        shootAt.x = x;
        shootAt.y = y;
        sendToServer(shootAt);
    }

    @Override
    public void reload() {
        sendToServer(new Messages.Reload());
    }

    void spawnAt(Point point) {
        Messages.SpawnRequest spawnRequest = new Messages.SpawnRequest();
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
