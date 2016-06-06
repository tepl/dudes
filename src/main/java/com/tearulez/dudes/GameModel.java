package com.tearulez.dudes;

import java.util.HashMap;
import java.util.Map;

public class GameModel {

    private HashMap<Integer, Position> positions = new HashMap<>();
    private Map<Integer, Network.MovePlayer> actions = new HashMap<>();
    private int nextPlayerId;

    public synchronized void bufferAction(int playerId, Network.MovePlayer move) {
        actions.put(playerId, move);
    }

    public synchronized int registerNewPlayer() {
        int playerId = nextPlayerId;
        positions.put(playerId, new Position());
        nextPlayerId += 1;
        return playerId;
    }

    public synchronized void removePlayer(int playerId) {
        positions.remove(playerId);
    }

    public synchronized void applyActions() {
        for (Map.Entry<Integer, Network.MovePlayer> action : actions.entrySet()) {
            int playerId = action.getKey();
            Network.MovePlayer move = action.getValue();
            Position position = positions.get(playerId);
            position.x += move.dx;
            position.y += move.dy;
        }
        actions.clear();
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<Integer, Position> getPositions() {
        return (Map<Integer, Position>) positions.clone();
    }
}
