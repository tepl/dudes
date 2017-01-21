package com.tearulez.dudes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StateSnapshot {
    private Map<Integer, Player> players;
    private List<Wall> walls;
    private List<Point> bullets;

    private StateSnapshot() {
    }

    static StateSnapshot create(Map<Integer, Player> players, List<Wall> walls, List<Point> bullets) {
        StateSnapshot state = new StateSnapshot();
        state.players = players;
        state.walls = walls;
        state.bullets = bullets;
        return state;
    }

    static StateSnapshot empty() {
        return create(Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());
    }

    public Map<Integer, Player> getPlayers() {
        return players;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public List<Point> getBullets() {
        return bullets;
    }
}
