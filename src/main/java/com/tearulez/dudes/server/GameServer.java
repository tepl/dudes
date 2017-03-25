package com.tearulez.dudes.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.tearulez.dudes.model.GameModel;
import com.tearulez.dudes.Network;
import com.tearulez.dudes.Point;
import com.tearulez.dudes.StateSnapshot;
import com.tearulez.dudes.model.Player;
import com.tearulez.dudes.model.Wall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class GameServer {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);
    private static final int INITIAL_MOVE_ACTION_TTL = 3;

    private final GameModel gameModel;
    private final Server server;
    private int nextPlayerId;
    private Map<Integer, Point> spawnRequests = new HashMap<>();
    private List<Integer> playersToRemove = new ArrayList<>();

    private GameServer(GameModel gameModel, Server server) {
        this.gameModel = gameModel;
        this.server = server;
    }

    private void initListener() {
        // For consistency, the classes to be sent over the network are
        // registered by the same method for both the client and server.
        Network.register(server);
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                ((PlayerConnection) connection).playerId = registerNewPlayer();
            }

            @Override
            public void received(Connection c, Object object) {
                PlayerConnection connection = (PlayerConnection) c;

                if (object instanceof Network.MovePlayer) {
                    Network.MovePlayer action = (Network.MovePlayer) object;
                    connection.acceptMoveAction(action);
                } else if (object instanceof Network.ShootAt) {
                    Network.ShootAt action = (Network.ShootAt) object;
                    connection.acceptShootAction(action);
                } else if (object instanceof Network.SpawnRequest) {
                    Network.SpawnRequest action = (Network.SpawnRequest) object;
                    spawnPlayer(connection.playerId, action.startingPosition);
                } else if (object instanceof Network.Reload) {
                    connection.acceptReloadAction((Network.Reload) object);
                }
            }

            @Override
            public void disconnected(Connection c) {
                int playerId = ((PlayerConnection) c).playerId;
                removePlayer(playerId);
            }
        });
    }

    private synchronized int registerNewPlayer() {
        int playerId = nextPlayerId;
        nextPlayerId += 1;
        return playerId;
    }

    private synchronized void spawnPlayer(int playerId, Point startingPosition) {
        spawnRequests.put(playerId, startingPosition);
    }

    private synchronized void removePlayer(int playerId) {
        playersToRemove.add(playerId);
    }

    private void startGameLoop() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        log.info("Starting game loop");
        Runnable runnable = () -> {
            try {
                synchronized (GameServer.this) {
                    HashMap<Integer, Network.MovePlayer> moveActions = collectMoveActions();
                    HashMap<Integer, Network.ShootAt> shootActions = collectShootActions();
                    Set<Integer> reloadingPlayers = collectReloadingPlayers();
                    gameModel.nextStep(spawnRequests, playersToRemove, moveActions, shootActions, reloadingPlayers);
                    spawnRequests.clear();
                    playersToRemove.clear();
                }
                log.debug("sending updated model to clients");

                sendStateSnapshots();

                Network.SpawnResponse spawnResponse = new Network.SpawnResponse();
                spawnResponse.success = true;
                for (Integer playerId : gameModel.getSpawnedPlayers()) {
                    PlayerConnection playerConnection = getPlayerConnectionByPlayerId(playerId);
                    server.sendToTCP(playerConnection.getID(), spawnResponse);
                }
                spawnResponse.success = false;
                for (Integer playerId : gameModel.getFailedToSpawnPlayers()) {
                    PlayerConnection playerConnection = getPlayerConnectionByPlayerId(playerId);
                    server.sendToTCP(playerConnection.getID(), spawnResponse);
                }

                Network.PlayerDeath death = new Network.PlayerDeath();
                for (Integer playerId : gameModel.getKilledPlayers()) {
                    PlayerConnection playerConnection = getPlayerConnectionByPlayerId(playerId);
                    server.sendToTCP(playerConnection.getID(), death);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        };
        scheduler.scheduleAtFixedRate(
                runnable,
                0,
                (int) (GameModel.TIME_STEP * 1000),
                TimeUnit.MILLISECONDS
        );
    }

    private void sendStateSnapshots() {
        Map<Integer, Player> players = gameModel.getPlayers();
        for (PlayerConnection connection : playerConnections()) {
            Optional<Player> player = Optional.ofNullable(players.get(connection.playerId));
            List<Player> otherPlayers = players.entrySet().stream()
                    .filter(entry -> entry.getKey() != connection.playerId)
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            Network.UpdateModel updateModel = new Network.UpdateModel();
            updateModel.stateSnapshot = StateSnapshot.create(
                    player,
                    otherPlayers,
                    gameModel.getWalls(),
                    gameModel.getBulletPositions(),
                    gameModel.wasDryFire(),
                    gameModel.wasReloading(),
                    gameModel.wasShot()
            );
            server.sendToTCP(connection.getID(), updateModel);
        }
    }

    private PlayerConnection getPlayerConnectionByPlayerId(Integer playerId) {
        for (PlayerConnection connection : playerConnections()) {
            if (connection.playerId == playerId) {
                return connection;
            }
        }
        throw new IllegalArgumentException("no player connection for player id: " + playerId);
    }

    private Collection<PlayerConnection> playerConnections() {
        return Arrays.stream(server.getConnections())
                .map(PlayerConnection.class::cast)
                .collect(Collectors.toList());
    }

    private HashMap<Integer, Network.MovePlayer> collectMoveActions() {
        HashMap<Integer, Network.MovePlayer> moveActions = new HashMap<>();
        for (PlayerConnection connection : playerConnections()) {
            connection.moveAction().ifPresent(
                    movePlayer -> moveActions.put(connection.playerId, movePlayer)
            );
        }
        return moveActions;
    }

    private HashMap<Integer, Network.ShootAt> collectShootActions() {
        HashMap<Integer, Network.ShootAt> shootActions = new HashMap<>();
        for (PlayerConnection connection : playerConnections()) {
            connection.shootAction().ifPresent(
                    shootAction -> shootActions.put(connection.playerId, shootAction)
            );
        }
        return shootActions;
    }

    private Set<Integer> collectReloadingPlayers() {
        Set<Integer> players = new HashSet<>();
        for (PlayerConnection connection : playerConnections()) {
            connection.reloadAction().ifPresent(
                    action -> players.add(connection.playerId)
            );
        }
        return players;
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

        ArrayList<Point> points4 = new ArrayList<>();
        points4.add(Point.create(80, 40));
        points4.add(Point.create(0, 10));
        points4.add(Point.create(40, 0));
        walls.add(Wall.create(Point.create(-20, -30), points4));

        ArrayList<Point> points5 = new ArrayList<>();
        points5.add(Point.create(20, 40));
        points5.add(Point.create(10, 40));
        points5.add(Point.create(50, 0));
        walls.add(Wall.create(Point.create(-20, 10), points5));

        ArrayList<Point> points6 = new ArrayList<>();
        points6.add(Point.create(15, 20));
        points6.add(Point.create(20, 10));
        points6.add(Point.create(40, 0));
        walls.add(Wall.create(Point.create(20, 10), points6));

        GameModel gameModel = GameModel.create(walls);
        Server server = new Server(Network.WRITE_BUFFER_SIZE, Network.MAX_OBJECT_SIZE) {
            protected Connection newConnection() {
                return new PlayerConnection(INITIAL_MOVE_ACTION_TTL);
            }
        };
        return new GameServer(gameModel, server);
    }

    public static void main(String[] args) throws IOException {
        Log.INFO();
        GameServer gameServer = GameServer.createServer();
        gameServer.initListener();
        gameServer.startGameLoop();
        gameServer.startServing(Integer.valueOf(args[0]));
    }
}
