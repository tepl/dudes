package com.tearulez.dudes;

import java.util.List;
import java.util.Map;

public class GameState {
    private Map<Integer, Point> positions;
    private List<Wall> walls;

    public GameState() {
    }

    public static GameState create(Map<Integer, Point> positions, List<Wall> walls) {
        GameState state = new GameState();
        state.positions = positions;
        state.walls = walls;
        return state;
    }

    public Map<Integer, Point> getPositions() {
        return positions;
    }

    public List<Wall> getWalls() {
        return walls;
    }

}
