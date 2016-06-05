package com.tearulez.dudes;

import java.util.HashMap;
import java.util.Map;

public class GameModel {
    private Map<Integer, Position> positions = new HashMap<>();
    private int playerId;
    private boolean initialized;

    public void init(int playerId) {
        this.playerId = playerId;
        positions.put(playerId, new Position());
        initialized = true;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Map<Integer, Position> getPositions() {
        return positions;
    }

    public void updateCharacter(int id, float x, float y) {
        Position p = positions.get(id);
        p.x = x;
        p.y = y;
    }

    public void movePlayer(float dx, float dy) {
        Position p = positions.get(playerId);
        p.x += dx;
        p.y += dy;
    }

    public boolean isInitialized() {
        return initialized;
    }
}