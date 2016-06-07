package com.tearulez.dudes;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameClient {
    private Client client;
    private Map<Integer, Position> positions = new HashMap<>();
    private int playerId;
    private volatile boolean initialized = false;

    public void init() {
        client = new Client();
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
                if (object instanceof Network.Registered) {
                    Network.Registered registered = (Network.Registered) object;
                    playerId = registered.id;
                    initialized = true;
                }

                // Update received
                if (object instanceof Network.UpdateModel) {
                    Network.UpdateModel updateModel = (Network.UpdateModel) object;
                    positions = updateModel.positions;
                }
            }

            public void disconnected(Connection connection) {
                System.exit(0);
            }
        }));
        try {
            client.connect(5000, "localhost", Network.port);
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

    public Map<Integer, Position> getPositions() {
        return positions;
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void closeServerConnection() {
        client.close();
    }
}
