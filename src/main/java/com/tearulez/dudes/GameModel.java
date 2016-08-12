package com.tearulez.dudes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class GameModel {

    static final float TIME_STEP = 1.0f / 60;
    static final float PLAYER_CIRCLE_SIZE = 1;
    static final float BULLER_CIRCLE_SIZE = 0.2f;

    private static final int VELOCITY_ITERATIONS = 8;
    private static final int POSITION_ITERATIONS = 3;
    private static final int MAX_BULLET_COUNT = 100;

    private Map<Integer, Body> playerBodies = new HashMap<>();
    private Queue<Body> bulletBodies = new ArrayDeque<>();
    private static World world = new World(new Vector2(0, 0), true);
    private ArrayList<Wall> walls = new ArrayList<>();
    private Map<Integer, Network.MovePlayer> moveActions = new HashMap<>();
    private Map<Integer, Network.ShootAt> shootActions = new HashMap<>();
    private int nextPlayerId;

    private GameModel() {
    }

    static GameModel create(ArrayList<Wall> walls) {
        GameModel gameModel = new GameModel();
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
        Body body = createCircleBody(PLAYER_CIRCLE_SIZE, new Vector2());
        playerBodies.put(playerId, body);
        return playerId;
    }

    private Body createCircleBody(float circleSize, Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        Body body = world.createBody(bodyDef);
        CircleShape shape = new CircleShape();
        shape.setRadius(circleSize);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        body.createFixture(fixtureDef);

        shape.dispose();
        return body;
    }

    synchronized void removePlayer(int playerId) {
        Body body = playerBodies.get(playerId);
        world.destroyBody(body);
        playerBodies.remove(playerId);
    }

    synchronized void nextStep() {
        for (Map.Entry<Integer, Network.MovePlayer> action : moveActions.entrySet()) {
            int playerId = action.getKey();
            Network.MovePlayer move = action.getValue();
            Body body = playerBodies.get(playerId);
            body.applyForceToCenter(move.dx * 5, move.dy * 5, true);
        }
        moveActions.clear();

        for (Map.Entry<Integer, Network.ShootAt> action : shootActions.entrySet()) {
            int playerId = action.getKey();
            Network.ShootAt shootAt = action.getValue();
            Vector2 target = new Vector2(shootAt.x, shootAt.y);
            Vector2 playerPosition = playerBodies.get(playerId).getPosition();
            Vector2 aim = target.cpy().sub(playerPosition).nor();
            // the offset is needed to eliminate bullet-shooter collision
            Vector2 offset = aim.scl(PLAYER_CIRCLE_SIZE + BULLER_CIRCLE_SIZE);
            Body bullet = createCircleBody(BULLER_CIRCLE_SIZE, playerPosition.cpy().add(offset));
            Vector2 bulletVelocity = aim.cpy().scl(15);
            bullet.setLinearVelocity(bulletVelocity);
            bulletBodies.add(bullet);
            if (bulletBodies.size() > MAX_BULLET_COUNT) {
                world.destroyBody(bulletBodies.remove());
            }
        }
        shootActions.clear();

        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }

    synchronized GameState getState() {
        return GameState.create(getPlayerPositions(), walls, getBulletPositions());
    }

    private Map<Integer, Point> getPlayerPositions() {
        Map<Integer, Point> positions = new HashMap<>();
        for (Map.Entry<Integer, Body> entry : playerBodies.entrySet()) {
            int playerId = entry.getKey();
            Vector2 center = entry.getValue().getPosition();
            Point p = Point.create(center.x, center.y);
            positions.put(playerId, p);
        }
        return positions;
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
}
