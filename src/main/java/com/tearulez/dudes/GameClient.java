package com.tearulez.dudes;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

class GameClient implements PlayerControls {
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
            public void connected(Connection connection) {
                client.sendTCP(new Network.Login());
            }

            public void received(Connection connection, Object object) {
                // Player registered
                if (object instanceof Network.Respawned) {
                    Network.Respawned respawned = (Network.Respawned) object;
                    game.onPlayerRespawn(respawned.id);
                }

                // Update received
                if (object instanceof Network.UpdateModel) {
                    Network.UpdateModel updateModel = (Network.UpdateModel) object;
                    game.onGameStateUpdate(updateModel.stateSnapshot);
                }

                // Player died
                if (object instanceof Network.PlayerDeath) {
                    game.onPlayerDeath();
                }
            }

            public void disconnected(Connection connection) {
                System.exit(0);
            }
        }));
        try {
            client.connect(5000, serverHost, serverPort);
            // Server communication after connection can go here, or in Listener#connected().
        } catch (IOException ex) {
            ex.printStackTrace();
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

    void closeServerConnection() {
        client.close();
    }

    void respawnAt(Point point) {
        Network.RespawnRequest respawnRequest = new Network.RespawnRequest();
        respawnRequest.startingPosition = point;
        client.sendTCP(respawnRequest);
    }
}
