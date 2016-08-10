package com.tearulez.dudes;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class GameServer {

    private final GameModel gameModel;
    private final Server server;

    private GameServer(GameModel gameModel, Server server) {
        this.gameModel = gameModel;
        this.server = server;
    }

    private void initListener() {
        // For consistency, the classes to be sent over the network are
        // registered by the same method for both the client and server.
        Network.register(server);
        server.addListener(new Listener() {
            public void received(Connection c, Object object) {
                PlayerConnection connection = (PlayerConnection) c;

                if (object instanceof Network.Login) {
                    Network.Registered registered = new Network.Registered();
                    int newPlayerId = gameModel.registerNewPlayer();
                    registered.id = newPlayerId;
                    connection.playerId = newPlayerId;
                    server.sendToTCP(c.getID(), registered);
                }

                if (object instanceof Network.MovePlayer) {
                    Network.MovePlayer movePlayer = (Network.MovePlayer) object;
                    gameModel.bufferAction(connection.playerId, movePlayer);
                }
            }

            public void disconnected(Connection c) {
                gameModel.removePlayer(((PlayerConnection) c).playerId);
            }
        });
    }

    private void startGameLoop() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable runnable = () -> {
            try {
                gameModel.nextStep();
                Network.UpdateModel updateModel = new Network.UpdateModel();
                updateModel.state = gameModel.getState();
                server.sendToAllTCP(updateModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(
                runnable,
                0,
                (int) (GameModel.TIME_STEP * 1000),
                TimeUnit.MILLISECONDS
        );
    }

    private void startServing() throws IOException {
        server.bind(Network.port);
        server.start();
    }

    private static GameServer createServer() throws IOException {
        ArrayList<Wall> walls = new ArrayList<>();

        ArrayList<Point> points1 = new ArrayList<>();
        points1.add(Point.create(-5, -5));
        points1.add(Point.create(5, 5));
        points1.add(Point.create(5, -5));
        walls.add(Wall.create(Point.create(15, -5), points1));

        ArrayList<Point> points2 = new ArrayList<>();
        points2.add(Point.create(0, 0));
        points2.add(Point.create(0, 20));
        points2.add(Point.create(10, 0));
        walls.add(Wall.create(Point.create(-20, -10), points2));

        ArrayList<Point> points3 = new ArrayList<>();
        points3.add(Point.create(0, 0));
        points3.add(Point.create(0, 5));
        points3.add(Point.create(40, 5));
        walls.add(Wall.create(Point.create(-20, 10), points3));

        GameModel gameModel = GameModel.create(walls);
        Server server = new Server() {
            protected Connection newConnection() {
                // By providing our own connection implementation, we can store per
                // connection state without a connection ID to state look up.
                return new PlayerConnection();
            }
        };
        return new GameServer(gameModel, server);
    }

    // This holds per connection state.
    private static class PlayerConnection extends Connection {
        int playerId;
    }

    public static void main(String[] args) throws IOException {
        Log.set(Log.LEVEL_DEBUG);
        GameServer gameServer = GameServer.createServer();
        gameServer.initListener();
        gameServer.startGameLoop();
        gameServer.startServing();
    }
}
