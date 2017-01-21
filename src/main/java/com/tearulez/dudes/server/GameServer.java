package com.tearulez.dudes.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.tearulez.dudes.GameModel;
import com.tearulez.dudes.Network;
import com.tearulez.dudes.Point;
import com.tearulez.dudes.Wall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class GameServer {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);
    private static final int INITIAL_MOVE_ACTION_TTL = 3;

    private final GameModel gameModel;
    private final Server server;
    private int nextPlayerId;
    private List<Integer> newPlayers = new ArrayList<>();
    private List<Integer> playersToRemove = new ArrayList<>();
    private Map<Integer, Network.MovePlayer> moveActions = new HashMap<>();
    private Map<Integer, Network.ShootAt> shootActions = new HashMap<>();
    private Map<Integer, Integer> moveActionsTTLs = new HashMap<>();
    private Map<Integer, Connection> playerIdToConnection = new ConcurrentHashMap<>();

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
                    Network.Respawned respawned = new Network.Respawned();
                    int newPlayerId = registerNewPlayer();
                    respawned.id = newPlayerId;
                    connection.playerId = newPlayerId;
                    playerIdToConnection.put(newPlayerId, connection);
                    server.sendToTCP(c.getID(), respawned);
                }

                if (object instanceof Network.MovePlayer) {
                    Network.MovePlayer action = (Network.MovePlayer) object;
                    bufferMoveAction(connection.playerId, action);
                }

                if (object instanceof Network.ShootAt) {
                    Network.ShootAt action = (Network.ShootAt) object;
                    bufferShootAction(connection.playerId, action);
                }

                if (object instanceof Network.RespawnRequest) {
                    addPlayer(connection.playerId);
                    Network.Respawned respawned = new Network.Respawned();
                    respawned.id = connection.playerId;
                    server.sendToTCP(c.getID(), respawned);
                }
            }

            public void disconnected(Connection c) {
                int playerId = ((PlayerConnection) c).playerId;
                removePlayer(playerId);
                playerIdToConnection.remove(playerId);
            }
        });
    }

    private synchronized int registerNewPlayer() {
        int playerId = nextPlayerId;
        nextPlayerId += 1;
        addPlayer(playerId);
        return playerId;
    }

    private synchronized void addPlayer(int playerId) {
        newPlayers.add(playerId);
    }

    private synchronized void removePlayer(int playerId) {
        playersToRemove.add(playerId);
    }

    private synchronized void bufferMoveAction(int playerId, Network.MovePlayer action) {
        moveActions.put(playerId, action);
        moveActionsTTLs.put(playerId, INITIAL_MOVE_ACTION_TTL);
    }

    private synchronized void bufferShootAction(int playerId, Network.ShootAt action) {
        shootActions.put(playerId, action);
    }

    private void startGameLoop() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        log.info("Starting game loop");
        Runnable runnable = () -> {
            try {
                synchronized (GameServer.this) {
                    gameModel.nextStep(newPlayers, playersToRemove, moveActions, shootActions);
                    newPlayers.clear();
                    playersToRemove.clear();
                    for (Integer playerId : updateMoveActionsTTLs()) {
                        moveActions.remove(playerId);
                    }
                    shootActions.clear();
                }
                Network.UpdateModel updateModel = new Network.UpdateModel();
                updateModel.stateSnapshot = gameModel.getStateSnapshot();
                log.debug("sending updated model to clients");
                server.sendToAllTCP(updateModel);

                Network.PlayerDeath death = new Network.PlayerDeath();
                for (Integer playerId : gameModel.getKilledPlayers()) {
                    server.sendToTCP(playerIdToConnection.get(playerId).getID(), death);
                }
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

    private List<Integer> updateMoveActionsTTLs() {
        Map<Integer, Integer> newMoveActionsTTLs = new HashMap<>();
        List<Integer> moveActionsToRemove = new ArrayList<>();
        for (Integer playerId : moveActionsTTLs.keySet()) {
            Integer ttl = moveActionsTTLs.get(playerId) - 1;
            if (ttl > 0) {
                newMoveActionsTTLs.put(playerId, ttl);
            } else {
                moveActionsToRemove.add(playerId);
            }
        }
        moveActionsTTLs = newMoveActionsTTLs;
        return moveActionsToRemove;
    }

    private void startServing(int port) throws IOException {
        server.bind(port);
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
        Server server = new Server(Network.WRITE_BUFFER_SIZE, Network.MAX_OBJECT_SIZE) {
            protected Connection newConnection() {
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
        Log.INFO();
        GameServer gameServer = GameServer.createServer();
        gameServer.initListener();
        gameServer.startGameLoop();
        gameServer.startServing(Integer.valueOf(args[0]));
    }
}
