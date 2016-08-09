package com.tearulez.dudes;

import java.util.List;
import java.util.Map;

class GameState {
    private Map<Integer, Point> positions;
    private List<Wall> walls;

    private GameState() {
    }

    static GameState create(Map<Integer, Point> positions, List<Wall> walls) {
        GameState state = new GameState();
        state.positions = positions;
        state.walls = walls;
        return state;
    }

    Map<Integer, Point> getPositions() {
        return positions;
    }

    List<Wall> getWalls() {
        return walls;
    }

}
