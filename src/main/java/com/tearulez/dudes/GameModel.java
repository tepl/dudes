package com.tearulez.dudes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class GameModel {

    static final float TIME_STEP = 1.0f / 60;
    static final float PLAYER_CIRCLE_RADIUS = 1;
    static final float BULLER_CIRCLE_RADIUS = 0.2f;

    private static final int VELOCITY_ITERATIONS = 8;
    private static final int POSITION_ITERATIONS = 3;
    private static final int MAX_BULLET_COUNT = 100;

    private Map<Integer, Body> playerBodies = new HashMap<>();
    private Map<Integer, Integer> playerHealths = new HashMap<>();
    private Queue<Body> bulletBodies = new ArrayDeque<>();

    private World world;
    private ArrayList<Wall> walls = new ArrayList<>();
    private Map<Integer, Network.MovePlayer> moveActions = new HashMap<>();
    private Map<Integer, Network.ShootAt> shootActions = new HashMap<>();
    private List<Body> bodiesToDestroy = new ArrayList<>();
    private int nextPlayerId;

    private GameModel(World world) {
        this.world = world;
    }

    static GameModel create(ArrayList<Wall> walls) {
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

    synchronized void bufferMoveAction(int playerId, Network.MovePlayer action) {
        moveActions.put(playerId, action);
    }

    synchronized void bufferShootAction(int playerId, Network.ShootAt action) {
        shootActions.put(playerId, action);
    }

    synchronized int registerNewPlayer() {
        int playerId = nextPlayerId;
        nextPlayerId += 1;
        Body body = createCircleBody(PLAYER_CIRCLE_RADIUS, new Vector2());
        body.setUserData(PlayerId.create(playerId));
        playerBodies.put(playerId, body);
        playerHealths.put(playerId, Player.MAX_HEALTH);
        return playerId;
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

    synchronized void removePlayer(int playerId) {
        Body body = playerBodies.get(playerId);
        bodiesToDestroy.add(body);
        playerBodies.remove(playerId);
        playerHealths.remove(playerId);
    }

    synchronized void nextStep() {
        for (Map.Entry<Integer, Network.MovePlayer> action : moveActions.entrySet()) {
            int playerId = action.getKey();
            Network.MovePlayer move = action.getValue();
            Body body = playerBodies.get(playerId);
            // Do not apply force if player was killed or disconnected
            if (body != null) {
                body.applyForceToCenter(move.dx * 5, move.dy * 5, true);
            }
        }
        moveActions.clear();

        for (Map.Entry<Integer, Network.ShootAt> action : shootActions.entrySet()) {
            int playerId = action.getKey();
            Network.ShootAt shootAt = action.getValue();
            Vector2 target = new Vector2(shootAt.x, shootAt.y);
            Body body = playerBodies.get(playerId);

            // Do not apply force if player was killed or disconnected
            if (body == null) {
                continue;
            }

            Vector2 playerPosition = body.getPosition();
            Vector2 aim = target.cpy().sub(playerPosition).nor();
            // the offset is needed to eliminate bullet-shooter collision
            Vector2 offset = aim.scl(PLAYER_CIRCLE_RADIUS + 3 * BULLER_CIRCLE_RADIUS);
            Body bullet = createCircleBody(BULLER_CIRCLE_RADIUS, playerPosition.cpy().add(offset));
            bullet.setUserData(new Bullet());
            Vector2 bulletVelocity = aim.cpy().scl(15);
            bullet.setLinearVelocity(bulletVelocity);
            bulletBodies.add(bullet);
            if (bulletBodies.size() > MAX_BULLET_COUNT) {
                world.destroyBody(bulletBodies.remove());
            }
        }
        shootActions.clear();

        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        for (Body body : bodiesToDestroy) {
            world.destroyBody(body);
        }
        bodiesToDestroy.clear();
    }

    synchronized GameState getState() {
        return GameState.create(getPlayers(), walls, getBulletPositions());
    }

    private Map<Integer, Player> getPlayers() {
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

    private List<Point> getBulletPositions() {
        Stream<Point> bullets = bulletBodies.stream().map(
                (bullet) -> {
                    Vector2 center = bullet.getPosition();
                    return Point.create(center.x, center.y);
                }
        );
        return bullets.collect(Collectors.toList());
    }

    private void processHit(Body playerBody, Body bulletBody) {
        int playerId = ((PlayerId) playerBody.getUserData()).getPlayerId();
        Vector2 velocity = playerBody.getLinearVelocity().cpy().sub(bulletBody.getLinearVelocity());
        int health = playerHealths.get(playerId) - (int) velocity.len2() / 10;
        if (health < 0) {
            removePlayer(playerId);
        } else {
            playerHealths.put(playerId, health);
        }
    }

    private class ListenerClass implements ContactListener {
        @Override
        public void beginContact(Contact contact) {
            Body bodyA = contact.getFixtureA().getBody();
            Body bodyB = contact.getFixtureB().getBody();

            if (bodyA.getUserData() instanceof PlayerId && bodyB.getUserData() instanceof Bullet) {
                processHit(bodyA, bodyB);
            } else if (bodyB.getUserData() instanceof PlayerId && bodyA.getUserData() instanceof Bullet) {
                processHit(bodyB, bodyA);
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

}
