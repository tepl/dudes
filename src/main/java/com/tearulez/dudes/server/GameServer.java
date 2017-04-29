package com.tearulez.dudes.server;

import com.tearulez.dudes.*;
import com.tearulez.dudes.model.GameModel;
import com.tearulez.dudes.model.GameModelConfig;
import com.tearulez.dudes.model.Player;
import com.tearulez.dudes.model.Wall;
import com.tearulez.dudes.networking.Connection;
import com.tearulez.dudes.networking.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class GameServer {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);
    private static final int INITIAL_MOVE_ACTION_TTL = 3;

    private final GameModel gameModel;
    private final Server server;
    private final AIEngine aiEngine;
    private int nextPlayerId;
    private final Map<Integer, Point> spawnRequests = new HashMap<>();
    private final List<Integer> playersToRemove = new ArrayList<>();
    private final Map<Integer, Connection> connections = new ConcurrentHashMap<>();
    private final Map<Integer, PlayerConnection> playerConnections = new ConcurrentHashMap<>();

    private GameServer(GameModel gameModel, Server server, AIEngine aiEngine) {
        this.gameModel = gameModel;
        this.server = server;
        this.aiEngine = aiEngine;
    }

    private synchronized int registerNewPlayer() {
        int playerId = nextPlayerId;
        nextPlayerId += 1;
        return playerId;
    }

    private synchronized void spawnPlayer(int playerId, Point startingPosition) {
        spawnRequests.put(playerId, startingPosition);
    }

    private void startGameLoop() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        log.info("Starting game loop");
        Runnable runnable = () -> {
            try {
                synchronized (GameServer.this) {
                    readFromAllClients();
                    HashMap<Integer, Network.MovePlayer> moveActions = collectMoveActions();
                    HashMap<Integer, Network.ShootAt> shootActions = collectShootActions();
                    Set<Integer> reloadingPlayers = collectReloadingPlayers();

                    spawnRequests.putAll(aiEngine.getSpawnRequests());
                    moveActions.putAll(aiEngine.getMoveActions());
                    shootActions.putAll(aiEngine.getShootActions());
                    reloadingPlayers.addAll(aiEngine.getReloadingPlayers());

                    cleanupConnections();

                    gameModel.nextStep(spawnRequests, playersToRemove, moveActions, shootActions, reloadingPlayers);
                    aiEngine.computeNextStep();

                    spawnRequests.clear();
                    playersToRemove.clear();
                }
                log.debug("sending updated model to clients");

                sendStateSnapshots();

                Network.SpawnResponse spawnResponse = new Network.SpawnResponse();
                spawnResponse.success = true;
                gameModel.getSpawnedPlayers().stream().filter(this::isRealPlayer).forEach(
                        playerId -> sendMessageToClient(playerId, spawnResponse)
                );

                spawnResponse.success = false;
                gameModel.getFailedToSpawnPlayers().stream().filter(this::isRealPlayer).forEach(
                        playerId -> sendMessageToClient(playerId, spawnResponse)
                );

                Network.PlayerDeath death = new Network.PlayerDeath();
                gameModel.getKilledPlayers().stream().filter(this::isRealPlayer).forEach(
                        playerId -> sendMessageToClient(playerId, death)
                );

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

    private void cleanupConnections() throws IOException {
        for (Integer playerId : playersToRemove) {
            connections.remove(playerId).close();
            playerConnections.remove(playerId);
        }
    }

    private void readFromAllClients() {
        connections.forEach((playerId, connection) -> {
            try {
                processClientMessages(playerId, connection.receive());
            } catch (IOException e) {
                removePlayer(playerId);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        });
    }

    private void processClientMessages(int playerId, List<Object> messages) {
        PlayerConnection connection = playerConnections.get(playerId);
        for (Object object : messages) {
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
    }

    private void sendStateSnapshots() {
        Map<Integer, Player> players = gameModel.getPlayers();
        connections.keySet().forEach((Integer playerId) -> {
            Optional<Player> player = Optional.ofNullable(players.get(playerId));
            List<Player> otherPlayers = players.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(playerId))
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
            sendMessageToClient(playerId, updateModel);
        });
    }

    private void sendMessageToClient(Integer playerId, Object updateModel) {
        try {
            connections.get(playerId).send(updateModel);
        } catch (IOException e) {
            removePlayer(playerId);
            e.printStackTrace();
        }
    }

    private void removePlayer(Integer playerId) {
        playersToRemove.add(playerId);
    }

    private boolean isRealPlayer(int id) {
        for (PlayerConnection c : playerConnections()) {
            if (c.playerId == id) return true;
        }
        return false;
    }

    private Collection<PlayerConnection> playerConnections() {
        return playerConnections.values();
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
        while (true) {
            try {
                Connection connection = server.accept();
                int playerId = registerNewPlayer();
                connections.put(playerId, connection);
                playerConnections.put(playerId, new PlayerConnection(INITIAL_MOVE_ACTION_TTL));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static GameServer createServer(GameModelConfig gameModelConfig) throws Exception {
        List<Wall> walls = new SvgMap(new File("maps/map.svg")).getWalls();
        GameModel gameModel = GameModel.create(walls, gameModelConfig);
        Server server = new Server();
        Rect spawnArea = new Rect(-50, 50, -50, 50);
        AIEngine aiEngine = new AIEngine(Arrays.asList(-1, -2, -3, -4, -5), spawnArea, gameModel);
        return new GameServer(gameModel, server, aiEngine);
    }

    public static void main(String[] args) throws Exception {
        // Game model config server
        ConfigServer configServer = new ConfigServer();
        configServer.startServing(Integer.valueOf(args[0]));

        // Game server
        GameServer gameServer = GameServer.createServer(configServer.getGameModelConfig());
        gameServer.startGameLoop();
        gameServer.startServing(Integer.valueOf(args[1]));
    }
}
