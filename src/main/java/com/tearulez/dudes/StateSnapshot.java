package com.tearulez.dudes;

import java.util.List;
import java.util.Map;

class StateSnapshot {
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

    Map<Integer, Player> getPlayers() {
        return players;
    }

    List<Wall> getWalls() {
        return walls;
    }

    List<Point> getBullets() {
        return bullets;
    }
}
