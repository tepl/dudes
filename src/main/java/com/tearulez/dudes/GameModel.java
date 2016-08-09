package com.tearulez.dudes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class GameModel {

    static final float TIME_STEP = 1.0f / 60;
    static final float PLAYER_CIRCLE_SIZE = 1;

    private static final int VELOCITY_ITERATIONS = 8;
    private static final int POSITION_ITERATIONS = 3;

    private HashMap<Integer, Body> bodies = new HashMap<>();
    private ArrayList<Wall> walls = new ArrayList<>();
    private Map<Integer, Network.MovePlayer> actions = new HashMap<>();
    private World world = new World(new Vector2(0, 0), true);
    private int nextPlayerId;

    GameModel(ArrayList<Wall> walls) {
        this.walls = walls;
    }

    synchronized void bufferAction(int playerId, Network.MovePlayer move) {
        actions.put(playerId, move);
    }

    synchronized int registerNewPlayer() {
        int playerId = nextPlayerId;
        nextPlayerId += 1;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(0, 0);
        Body body = world.createBody(bodyDef);
        CircleShape shape = new CircleShape();
        shape.setRadius(PLAYER_CIRCLE_SIZE);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        body.createFixture(fixtureDef);

        shape.dispose();

        bodies.put(playerId, body);
        return playerId;
    }

    synchronized void removePlayer(int playerId) {
        Body body = bodies.get(playerId);
        world.destroyBody(body);
        bodies.remove(playerId);
    }

    synchronized void nextStep() {
        for (Map.Entry<Integer, Network.MovePlayer> action : actions.entrySet()) {
            int playerId = action.getKey();
            Network.MovePlayer move = action.getValue();
            Body body = bodies.get(playerId);
            body.applyForceToCenter(move.dx * 5, move.dy * 5, true);
        }
        actions.clear();
        world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }

    synchronized GameState getState() {
        return GameState.create(getPositions(), walls);
    }

    private Map<Integer, Point> getPositions() {
        Map<Integer, Point> positions = new HashMap<>();
        for (Map.Entry<Integer, Body> entry : bodies.entrySet()) {
            int playerId = entry.getKey();
            Vector2 center = entry.getValue().getPosition();
            Point p = Point.create(center.x, center.y);
            positions.put(playerId, p);
        }
        return positions;
    }
}
