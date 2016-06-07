package com.tearulez.dudes;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameServer {

    private final GameModel gameModel;
    private final Server server;

    public GameServer(GameModel gameModel, Server server) {
        this.gameModel = gameModel;
        this.server = server;
    }

    public void initListener() {
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

    public void startGameLoop() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable runnable = () -> {
            gameModel.nextStep();
            Network.UpdateModel updateModel = new Network.UpdateModel();
            updateModel.positions = gameModel.getPositions();
            server.sendToAllTCP(updateModel);
        };
        scheduler.scheduleAtFixedRate(
                runnable,
                0,
                (int) (GameModel.TIME_STEP * 1000),
                TimeUnit.MILLISECONDS
        );
    }

    public void startServing() throws IOException {
        server.bind(Network.port);
        server.start();
    }

    public static GameServer createServer() throws IOException {
        GameModel gameModel = new GameModel();
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
    static class PlayerConnection extends Connection {
        public int playerId;
    }

    public static void main(String[] args) throws IOException {
        Log.set(Log.LEVEL_DEBUG);
        GameServer gameServer = GameServer.createServer();
        gameServer.initListener();
        gameServer.startGameLoop();
        gameServer.startServing();
    }
}
