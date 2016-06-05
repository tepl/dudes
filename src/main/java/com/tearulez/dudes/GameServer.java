package com.tearulez.dudes;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameServer {
    private Server server;
    private Map<Integer, Position> positions = new HashMap<>();
    private int nextPlayer = 0;

    public GameServer() throws IOException {
        server = new Server() {
            protected Connection newConnection() {
                // By providing our own connection implementation, we can store per
                // connection state without a connection ID to state look up.
                return new PlayerConnection();
            }
        };

        // For consistency, the classes to be sent over the network are
        // registered by the same method for both the client and server.
        Network.register(server);

        server.addListener(new Listener() {
            public void received(Connection c, Object object) {
                // We know all connections for this server are actually CharacterConnections.
                PlayerConnection connection = (PlayerConnection) c;
                int playerId = connection.playerId;

                if (object instanceof Network.Login) {
                    Network.Registered registered = new Network.Registered();
                    registered.id = ++nextPlayer;
                    positions.put(playerId, new Position());
                    server.sendToTCP(c.getID(), registered);
                    Network.UpdateModel updateModel = new Network.UpdateModel();

                    // Stub for update model
                    updateModel.positions = new HashMap<>();
                    updateModel.positions.put(playerId, new Position(-0.2f, -0.2f));
                    updateModel.positions.put(playerId + 1, new Position(0.3f, 0.5f));
                    server.sendToAllTCP(updateModel);
                }

                if (object instanceof Network.MovePlayer) {
                    Network.MovePlayer movePlayer = (Network.MovePlayer) object;
                    Position position = positions.get(((PlayerConnection) c).playerId);
                    position.x += movePlayer.dx;
                    position.y += movePlayer.dy;
                    System.out.println("Player " + playerId + " x: " + position.x + " y: " + position.y);
                }
            }

            public void disconnected(Connection c) {
                positions.remove(((PlayerConnection) c).playerId);
            }
        });
        server.bind(Network.port);
        server.start();
    }


    // This holds per connection state.
    static class PlayerConnection extends Connection {
        public int playerId;
    }

    public static void main(String[] args) throws IOException {
        Log.set(Log.LEVEL_DEBUG);
        new GameServer();
    }
}
