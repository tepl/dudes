package com.tearulez.dudes.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.tearulez.dudes.Network;
import com.tearulez.dudes.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tearulez.dudes.Assertions.assertState;

public class GameModel {
    private static final Logger log = LoggerFactory.getLogger(GameModel.class);

    private static final int TICKS_PER_SECOND = 60;
    public static final float TIME_STEP = 1.0f / TICKS_PER_SECOND;
    public static final float PLAYER_CIRCLE_RADIUS = 1;
    public static final float BULLET_CIRCLE_RADIUS = 0.2f;

    private static final int VELOCITY_ITERATIONS = 8;
    private static final int POSITION_ITERATIONS = 3;
    private static final int MAX_BULLET_COUNT = 100;

    private static final float MAX_SPEED = 10f;
    private static final int FORCE_SCALE = 100;
    private static final int BREAKING_FORCE = 20;
    private static final int MIN_SHOOTING_CYCLE_IN_TICKS = TICKS_PER_SECOND / 12;
    private static final int MAGAZINE_SIZE = 30;
    private static final int RELOAD_TIME_IN_TICKS = TICKS_PER_SECOND * 2;

    private Map<Integer, Body> playerBodies = new HashMap<>();
    private Map<Integer, Integer> playerHealths = new HashMap<>();
    private Map<Integer, Long> previousShootActionTicks = new HashMap<>();
    private Map<Integer, Long> previousReloadActionTicks = new HashMap<>();
    private Map<Integer, Integer> magazineAmmoCounts = new HashMap<>();
    private Queue<Body> bulletBodies = new ArrayDeque<>();

    private World world;
    private ArrayList<Wall> walls = new ArrayList<>();
    private List<Integer> killedPlayers = new ArrayList<>();
    private List<PlayerBulletCollision> collisions = new ArrayList<>();
    private boolean wasShot = false;
    private boolean wasDryFire = false;
    private boolean wasReloading = false;

    private long currentTick = 0;

    private GameModel(World world) {
        this.world = world;
    }

    public static GameModel create(ArrayList<Wall> walls) {
        World world = new World(new Vector2(0, 0), true);

        GameModel gameModel = new GameModel(world);
        world.setContactListener(gameModel.new ListenerClass());
        gameModel.walls = walls;
        for (Wall wall : walls) {
            Point position = wall.getPosition();
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(position.x, position.y);
            Body body = world.createBody(bodyDef);

            List<Point> points = wall.getPoints();
            int size = wall.getPoints().size();
            float[] vertices = new float[size * 2];
            for (int i = 0; i < size; i++) {
                Point point = points.get(i);
                vertices[i * 2] = point.x;
                vertices[i * 2 + 1] = point.y;
            }
            PolygonShape polygonShape = new PolygonShape();
            polygonShape.set(vertices);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = polygonShape;
            fixtureDef.density = 1f;
            body.createFixture(fixtureDef);

            polygonShape.dispose();
        }
        return gameModel;
    }

    private boolean isPlayerPresent(int playerId) {
        return playerBodies.containsKey(playerId);
    }

    public void nextStep(Map<Integer, Point> newPlayers,
                         List<Integer> playersToRemove,
                         Map<Integer, Network.MovePlayer> moveActions,
                         Map<Integer, Network.ShootAt> shootActions,
                         Set<Integer> reloadingPlayers) {
        checkInvariants();
        cleanUp();
        processNewPlayers(newPlayers);
        playersToRemove.forEach(this::removePlayer);
        processMoveActions(moveActions);
        processShootActions(shootActions);
        processReloading(reloadingPlayers);
        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        handlePlayerBulletCollisions();
        currentTick += 1;
    }

    private void checkInvariants() {
        if (log.isTraceEnabled()) {
            Array<Body> bodies = new Array<>();
            world.getBodies(bodies);
            int players = 0;
            int bullets = 0;
            for (Body body : bodies) {
                Object o = body.getUserData();
                if (o instanceof PlayerId) {
                    players += 1;
                }
                if (o instanceof Bullet) {
                    bullets += 1;
                }
            }
            log.trace(
                    "world body count {}, players {}, bullets {}, walls {}",
                    world.getBodyCount(), players, bullets, walls.size()
            );
        }

        assertState(
                world.getBodyCount() <= playerBodies.size() + walls.size() + MAX_BULLET_COUNT,
                "world body count is less than players count + walls count + max bullet count"
        );
    }

    private void handlePlayerBulletCollisions() {
        Map<Integer, List<PlayerBulletCollision>> collisionByPlayerId =
                collisions.stream()
                        .collect(Collectors.groupingBy(collision -> collision.playerId));
        collisionByPlayerId.forEach((playerId, collisions) -> {
            double sum = collisions.stream()
                    .mapToDouble(c -> c.squaredRelativeVelocity)
                    .sum();
            int health = playerHealths.get(playerId) - (int) sum / 10;
            if (health < 0) {
                removePlayer(playerId);
                killedPlayers.add(playerId);
            } else {
                playerHealths.put(playerId, health);
            }
        });
        collisions.clear();
    }

    private void cleanUp() {
        killedPlayers.clear();
        wasShot = false;
        wasDryFire = false;
        wasReloading = false;
    }

    private void processNewPlayers(Map<Integer, Point> newPlayers) {
        for (Map.Entry<Integer, Point> player : newPlayers.entrySet()) {
            Integer playerId = player.getKey();
            if (isPlayerPresent(playerId)) {
                log.warn("Ignoring new player, playerId: {}", playerId);
                continue;
            }
            log.debug("Add new player: {}", playerId);
            Point position = player.getValue();
            Body body = createCircleBody(PLAYER_CIRCLE_RADIUS, new Vector2(position.x, position.y));
            body.setUserData(PlayerId.create(playerId));
            playerBodies.put(playerId, body);
            playerHealths.put(playerId, Player.MAX_HEALTH);
            magazineAmmoCounts.put(playerId, MAGAZINE_SIZE);
        }
    }

    private void removePlayer(int playerId) {
        if (!isPlayerPresent(playerId)) {
            log.warn("Removing player that does not exist, playerId: {}", playerId);
            return;
        }
        log.debug("Remove player: {}", playerId);
        Body body = playerBodies.get(playerId);
        world.destroyBody(body);
        playerBodies.remove(playerId);
        playerHealths.remove(playerId);
        previousShootActionTicks.remove(playerId);
        previousReloadActionTicks.remove(playerId);
        magazineAmmoCounts.remove(playerId);
    }

    private Body createCircleBody(float circleRadius, Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        Body body = world.createBody(bodyDef);
        CircleShape shape = new CircleShape();
        shape.setRadius(circleRadius);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        body.createFixture(fixtureDef);

        shape.dispose();
        return body;
    }

    private void processMoveActions(Map<Integer, Network.MovePlayer> moveActions) {
        for (Integer playerId : getPlayerIds()) {
            Network.MovePlayer move = moveActions.get(playerId);
            Body body = playerBodies.get(playerId);
            if (move == null) {
                Vector2 brakingForce = body.getLinearVelocity().cpy().scl(-BREAKING_FORCE);
                body.applyForceToCenter(brakingForce, true);
            } else {
                Vector2 force = new Vector2(move.dx, move.dy);
                force.scl(FORCE_SCALE);
                if (body.getLinearVelocity().len() > MAX_SPEED) {
                    Vector2 heading = body.getLinearVelocity().cpy().nor();
                    float dot = force.dot(heading);
                    if (dot > 0) {
                        heading.scl(dot);
                        force.sub(heading);
                    }
                }
                body.applyForceToCenter(force, true);
            }
        }
    }

    private Iterable<Integer> getPlayerIds() {
        return playerBodies.keySet();
    }

    private void processShootActions(Map<Integer, Network.ShootAt> shootActions) {
        for (Map.Entry<Integer, Network.ShootAt> action : shootActions.entrySet()) {
            int playerId = action.getKey();
            if (!isPlayerPresent(playerId)) {
                continue;
            }
            // Fire rate limit
            Long previousShotTick = previousShootActionTicks.get(playerId);
            if (previousShotTick != null && currentTick - previousShotTick < MIN_SHOOTING_CYCLE_IN_TICKS) {
                continue;
            }
            previousShootActionTicks.put(playerId, currentTick);
            // Empty magazine
            Integer ammoCount = magazineAmmoCounts.get(playerId);
            assertState(ammoCount >= 0, "ammo count must be non-negative");
            if (ammoCount == 0) {
                wasDryFire = true;
                continue;
            }
            // Reloading
            Long previousReloadingTick = previousReloadActionTicks.get(playerId);
            if (previousReloadingTick != null && currentTick - previousReloadingTick < RELOAD_TIME_IN_TICKS) {
                continue;
            }
            Network.ShootAt shootAt = action.getValue();
            Vector2 target = new Vector2(shootAt.x, shootAt.y);
            Body body = playerBodies.get(playerId);

            Vector2 playerPosition = body.getPosition();
            Vector2 aim = target.cpy().sub(playerPosition);
            if (aim.len() < PLAYER_CIRCLE_RADIUS) {
                continue;
            }
            aim.nor();
            // the offset is needed to eliminate bullet-shooter collision
            Vector2 offset = aim.scl(PLAYER_CIRCLE_RADIUS + 3 * BULLET_CIRCLE_RADIUS);
            Body bullet = createCircleBody(BULLET_CIRCLE_RADIUS, playerPosition.cpy().add(offset));
            bullet.setUserData(new Bullet());
            Vector2 bulletVelocity = aim.cpy().scl(15);
            bullet.setLinearVelocity(bulletVelocity);
            bulletBodies.add(bullet);
            if (bulletBodies.size() > MAX_BULLET_COUNT) {
                world.destroyBody(bulletBodies.remove());
            }
            wasShot = true;
            magazineAmmoCounts.put(playerId, magazineAmmoCounts.get(playerId) - 1);
        }
    }

    private void processReloading(Set<Integer> players) {
        for (Integer playerId : players) {
            magazineAmmoCounts.put(playerId, MAGAZINE_SIZE);
            previousReloadActionTicks.put(playerId, currentTick);
            wasReloading = true;
        }
    }

    public Map<Integer, Player> getPlayers() {
        Map<Integer, Player> players = new HashMap<>();
        for (Map.Entry<Integer, Body> entry : playerBodies.entrySet()) {
            int playerId = entry.getKey();
            Vector2 center = entry.getValue().getPosition();
            Point position = Point.create(center.x, center.y);
            Player player = Player.create(position, playerHealths.get(playerId));
            players.put(playerId, player);
        }
        return players;
    }

    public ArrayList<Wall> getWalls() {
        return walls;
    }

    public List<Point> getBulletPositions() {
        Stream<Point> bullets = bulletBodies.stream().map(
                (bullet) -> {
                    Vector2 center = bullet.getPosition();
                    return Point.create(center.x, center.y);
                }
        );
        return bullets.collect(Collectors.toList());
    }

    private void queueCollisionEvent(Body playerBody, Body bulletBody) {
        int playerId = ((PlayerId) playerBody.getUserData()).getPlayerId();
        Vector2 relativeVelocity = playerBody.getLinearVelocity().cpy().sub(bulletBody.getLinearVelocity());
        collisions.add(new PlayerBulletCollision(playerId, relativeVelocity.len2()));
    }

    public List<Integer> getKilledPlayers() {
        return killedPlayers;
    }

    public boolean wasShot() {
        return wasShot;
    }

    public boolean wasDryFire() {
        return wasDryFire;
    }

    public boolean wasReloading() {
        return wasReloading;
    }

    private class ListenerClass implements ContactListener {
        @Override
        public void beginContact(Contact contact) {
            Body bodyA = contact.getFixtureA().getBody();
            Body bodyB = contact.getFixtureB().getBody();

            if (bodyA.getUserData() instanceof PlayerId && bodyB.getUserData() instanceof Bullet) {
                queueCollisionEvent(bodyA, bodyB);
            } else if (bodyB.getUserData() instanceof PlayerId && bodyA.getUserData() instanceof Bullet) {
                queueCollisionEvent(bodyB, bodyA);
            }
        }

        @Override
        public void endContact(Contact contact) {

        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }

    private class PlayerBulletCollision {
        final int playerId;
        final float squaredRelativeVelocity;

        PlayerBulletCollision(int playerId, float squaredRelativeVelocity) {
            this.playerId = playerId;
            this.squaredRelativeVelocity = squaredRelativeVelocity;
        }
    }
}
