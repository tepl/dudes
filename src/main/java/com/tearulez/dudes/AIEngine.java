package com.tearulez.dudes;

import com.badlogic.gdx.math.Vector2;
import com.tearulez.dudes.model.GameModel;
import com.tearulez.dudes.model.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AIEngine {
    private static final int MAX_MOVEMENT = 3;
    private List<Integer> aiPlayerIds;
    private final Rect spawnArea;
    private final GameModel gameModel;
    private Map<Integer, Point> spawnRequests = new HashMap<>();
    private Map<Integer, Network.MovePlayer> moveActions = new HashMap<>();
    private Map<Integer, Movement> movements = new HashMap<>();
    private Random rnd = new Random();

    private static class Movement {
        private final Vector2 direction;
        private int ticks;

        Movement(Vector2 direction, int ticks) {
            this.direction = direction;
            this.ticks = ticks;
        }
    }

    public AIEngine(List<Integer> aiPlayerIds, Rect spawnArea, GameModel gameModel) {
        this.aiPlayerIds = aiPlayerIds;
        this.spawnArea = spawnArea;
        this.gameModel = gameModel;
    }

    public void computeNextStep() {
        spawnRequests.clear();
        moveActions.clear();
        getDeadAIPlayerIds().forEach(
                id -> spawnRequests.put(id, spawnArea.getRandomPoint())
        );
        createMoveActions();
    }

    private List<Integer> getDeadAIPlayerIds() {
        Set<Integer> alivePlayers = gameModel.getPlayers().keySet();
        return aiPlayerIds.stream()
                .filter(id -> !alivePlayers.contains(id))
                .collect(Collectors.toList());
    }

    private Map<Integer, Player> getAliveAIPlayers() {
        Map<Integer, Player> players = gameModel.getPlayers();
        Map<Integer, Player> aiPlayers = new HashMap<>();
        players.forEach((id, player) -> {
            if (aiPlayerIds.contains(id)) {
                aiPlayers.put(id, player);
            }
        });
        return aiPlayers;
    }

    private void createMoveActions() {
        getAliveAIPlayers().forEach((id, player) -> {
            Movement movement = movements.get(id);
            if (movement != null && movement.ticks > 0) {
                Network.MovePlayer action = new Network.MovePlayer();
                action.dx = movement.direction.x;
                action.dy = movement.direction.y;
                movement.ticks -= 1;
                moveActions.put(id, action);
            } else {
                Point position = player.getPosition();
                Vector2 direction;
                if (spawnArea.contains(position)) {
                    direction = new Vector2(
                            rnd.nextFloat() - 0.5f,
                            rnd.nextFloat() - 0.5f
                    );
                } else {
                    Vector2 randomPoint = spawnArea.getRandomPoint().asVector();
                    direction = randomPoint.sub(position.asVector());
                }
                int ticks = rnd.nextInt(GameModel.TICKS_PER_SECOND * MAX_MOVEMENT);
                movements.put(id, new Movement(direction, ticks));
            }
        });
    }

    public Map<Integer, Point> getSpawnRequests() {
        return spawnRequests;
    }

    public Map<Integer, Network.MovePlayer> getMoveActions() {
        return moveActions;
    }
}
