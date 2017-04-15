package com.tearulez.dudes.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.tearulez.dudes.*;
import com.tearulez.dudes.model.GameModel;
import com.tearulez.dudes.model.Player;
import com.tearulez.dudes.model.Wall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    private final NPCEngine npcEngine;
    private int nextPlayerId;
    private Map<Integer, Point> spawnRequests = new HashMap<>();
    private List<Integer> playersToRemove = new ArrayList<>();

    private GameServer(GameModel gameModel, Server server, NPCEngine npcEngine) {
        this.gameModel = gameModel;
        this.server = server;
        this.npcEngine = npcEngine;
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

                    spawnRequests.putAll(npcEngine.getSpawnRequests());

                    gameModel.nextStep(spawnRequests, playersToRemove, moveActions, shootActions, reloadingPlayers);
                    npcEngine.computeNextStep();

                    spawnRequests.clear();
                    playersToRemove.clear();
                }
                log.debug("sending updated model to clients");

                sendStateSnapshots();

                Network.SpawnResponse spawnResponse = new Network.SpawnResponse();
                spawnResponse.success = true;
                gameModel.getSpawnedPlayers().stream().filter(this::isRealPlayer).forEach(playerId -> {
                    PlayerConnection playerConnection = getPlayerConnectionByPlayerId(playerId);
                    server.sendToTCP(playerConnection.getID(), spawnResponse);
                });

                spawnResponse.success = false;
                gameModel.getFailedToSpawnPlayers().stream().filter(this::isRealPlayer).forEach(playerId -> {
                    PlayerConnection playerConnection = getPlayerConnectionByPlayerId(playerId);
                    server.sendToTCP(playerConnection.getID(), spawnResponse);
                });

                Network.PlayerDeath death = new Network.PlayerDeath();
                gameModel.getKilledPlayers().stream().filter(this::isRealPlayer).forEach(playerId -> {
                    PlayerConnection playerConnection = getPlayerConnectionByPlayerId(playerId);
                    server.sendToTCP(playerConnection.getID(), death);
                });

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

    private boolean isRealPlayer(int id) {
        for (PlayerConnection c : playerConnections()) {
            if (c.playerId == id) return true;
        }
        return false;
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

    private static GameServer createServer() throws Exception {
        List<Wall> walls = new SvgMap(new File("maps/map.svg")).getWalls();
        GameModel gameModel = GameModel.create(walls);
        Server server = new Server(Network.WRITE_BUFFER_SIZE, Network.MAX_OBJECT_SIZE) {
            protected Connection newConnection() {
                return new PlayerConnection(INITIAL_MOVE_ACTION_TTL);
            }
        };
        Rect spawnArea = new Rect(-50, 50, -50, 50);
        NPCEngine npcEngine = new NPCEngine(Arrays.asList(-1, -2, -3, -4, -5), spawnArea, gameModel);
        return new GameServer(gameModel, server, npcEngine);
    }

    public static void main(String[] args) throws Exception {
        Log.INFO();
        GameServer gameServer = GameServer.createServer();
        gameServer.initListener();
        gameServer.startGameLoop();
        gameServer.startServing(Integer.valueOf(args[0]));
    }
}
