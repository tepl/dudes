package com.tearulez.dudes;

import java.util.List;
import java.util.Map;

class GameState {
    private Map<Integer, Point> positions;
    private List<Wall> walls;
    private List<Point> bullets;

    private GameState() {
    }

    static GameState create(Map<Integer, Point> positions, List<Wall> walls, List<Point> bullets) {
        GameState state = new GameState();
        state.positions = positions;
        state.walls = walls;
        state.bullets = bullets;
        return state;
    }

    Map<Integer, Point> getPositions() {
        return positions;
    }

    List<Wall> getWalls() {
        return walls;
    }

    List<Point> getBullets() {
        return bullets;
    }
}
