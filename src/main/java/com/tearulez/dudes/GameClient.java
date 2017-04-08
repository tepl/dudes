package com.tearulez.dudes;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class GameClient implements PlayerControls {
    private static final Logger log = LoggerFactory.getLogger(GameClient.class);
    private final String serverHost;
    private final int serverPort;
    private Client client;

    GameClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    void init(DudesGame game) {
        client = new Client(Network.WRITE_BUFFER_SIZE, Network.MAX_OBJECT_SIZE);
        client.start();

        // For consistency, the classes to be sent over the network are
        // registered by the same method for both the client and server.
        Network.register(client);

        // ThreadedListener runs the listener methods on a different thread.
        client.addListener(new Listener.ThreadedListener(new Listener() {

            @Override
            public void received(Connection connection, Object object) {
                // Spawn response
                if (object instanceof Network.SpawnResponse) {
                    Network.SpawnResponse spawnResponse = (Network.SpawnResponse) object;
                    log.info("onPlayerSpawn: " + spawnResponse.success);
                    game.onPlayerSpawn(spawnResponse.success);
                }

                // Update received
                if (object instanceof Network.UpdateModel) {
                    Network.UpdateModel updateModel = (Network.UpdateModel) object;
                    log.trace("onGameStateUpdate");
                    game.onGameStateUpdate(updateModel.stateSnapshot);
                }

                // Player died
                if (object instanceof Network.PlayerDeath) {
                    log.info("onPlayerDeath");
                    game.onPlayerDeath();
                }
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("Disconnected from server");
                Gdx.app.exit();
            }
        }));
        try {
            client.connect(5000, serverHost, serverPort);
            // Server communication after connection can go here, or in Listener#connected().
        } catch (IOException ex) {
            ex.printStackTrace();
            Gdx.app.exit();
        }
    }

    public void movePlayer(float dx, float dy) {
        Network.MovePlayer movePlayer = new Network.MovePlayer();
        movePlayer.dx = dx;
        movePlayer.dy = dy;
        client.sendTCP(movePlayer);
    }

    public void shootAt(float x, float y) {
        Network.ShootAt shootAt = new Network.ShootAt();
        shootAt.x = x;
        shootAt.y = y;
        client.sendTCP(shootAt);
    }

    @Override
    public void reload() {
        client.sendTCP(new Network.Reload());
    }

    void closeServerConnection() {
        client.close();
    }

    void spawnAt(Point point) {
        Network.SpawnRequest spawnRequest = new Network.SpawnRequest();
        spawnRequest.startingPosition = point;
        client.sendTCP(spawnRequest);
    }
}
