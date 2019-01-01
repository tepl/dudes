package com.tearulez.dudes.server.engine;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tearulez.dudes.common.Messages;
import com.tearulez.dudes.common.snapshot.Player;
import com.tearulez.dudes.common.snapshot.Point;

import java.util.*;
import java.util.stream.Collectors;

public class AIEngine {
    private static final int MAX_MOVEMENT = 3;
    private final float VISION_DISTANCE = 30;
    private List<Integer> aiPlayerIds;
    private final Rect spawnArea;
    private final GameModel gameModel;
    private Map<Integer, Point> spawnRequests = new HashMap<>();
    private Map<Integer, Messages.MovePlayer> moveActions = new HashMap<>();
    private Map<Integer, Messages.RotatePlayer> rotationActions = new HashMap<>();
    private Map<Integer, Messages.ShootAt> shootActions = new HashMap<>();
    private Set<Integer> reloadingPlayers = new HashSet<>();
    private Map<Integer, Movement> movements = new HashMap<>();
    private Random rnd = new Random();
    private LeadCalculator leadCalculator;

    private static class Movement {
        private final Vector2 direction;
        private int ticks;

        Movement(Vector2 direction, int ticks) {
            this.direction = direction;
            this.ticks = ticks;
        }
    }

    public AIEngine(List<Integer> aiPlayerIds, Rect spawnArea, GameModel gameModel, float bulletSpeed) {
        this.aiPlayerIds = aiPlayerIds;
        this.spawnArea = spawnArea;
        this.gameModel = gameModel;
        leadCalculator = new LeadCalculator(bulletSpeed);
    }

    public void computeNextStep() {
        spawnRequests.clear();
        moveActions.clear();
        rotationActions.clear();
        shootActions.clear();
        reloadingPlayers.clear();
        getDeadAIPlayerIds().forEach(
                id -> spawnRequests.put(id, spawnArea.getRandomPoint())
        );
        createMoveActions();
        createShootReloadRotationActions();
    }

    private void createShootReloadRotationActions() {
        Map<Integer, Player> players = gameModel.getPlayers();
        getAliveAIPlayers().forEach((id, player) -> {
            Optional<Integer> randomPlayer = getRandomOnSightPlayer(id, players.keySet());
            randomPlayer.ifPresent(targetId -> {
                        Point playerPosition = players.get(id).getPosition();
                        Player target = players.get(targetId);
                        Vector2 aim = leadCalculator.calculateCollisionPoint(
                                playerPosition,
                                target.getPosition(),
                                target.getVelocity()
                        );
                        shootAtOrReload(id, aim);

                        // Add rotation action
                        Messages.RotatePlayer rotationAction = new Messages.RotatePlayer();
                        rotationAction.angle = aim.sub(playerPosition.asVector()).angle() * MathUtils.degreesToRadians;
                        rotationActions.put(id, rotationAction);
                    }
            );
        });
    }

    private void shootAtOrReload(Integer id, Vector2 targetPoint) {
        if (gameModel.isMagazineEmpty(id)) {
            reloadingPlayers.add(id);
        } else {
            Messages.ShootAt shootAction = new Messages.ShootAt();
            shootAction.x = targetPoint.x;
            shootAction.y = targetPoint.y;
            shootActions.put(id, shootAction);
        }
    }

    private Optional<Integer> getRandomOnSightPlayer(Integer id, Set<Integer> otherPlayers) {
        List<Integer> playersOnSight = otherPlayers.stream().filter(
                otherId -> !otherId.equals(id)
                        && gameModel.isOnLineOfSight(id, otherId)
                        && gameModel.getDistanceBetweenPlayers(id, otherId) < VISION_DISTANCE

        ).collect(Collectors.toList());
        if (playersOnSight.isEmpty()) {
            return Optional.empty();
        } else {
            int randomPlayer = playersOnSight.get(rnd.nextInt(playersOnSight.size()));
            return Optional.of(randomPlayer);
        }
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
                Messages.MovePlayer action = new Messages.MovePlayer();
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

    public Map<Integer, Messages.MovePlayer> getMoveActions() {
        return moveActions;
    }

    public Map<Integer, Messages.RotatePlayer> getRotationActions() {
        return rotationActions;
    }

    public Map<Integer, Messages.ShootAt> getShootActions() {
        return shootActions;
    }

    public Set<Integer> getReloadingPlayers() {
        return reloadingPlayers;
    }
}
