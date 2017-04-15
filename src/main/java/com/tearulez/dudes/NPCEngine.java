package com.tearulez.dudes;

import com.tearulez.dudes.model.GameModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NPCEngine {
    private List<Integer> NPCIds;
    private final Rect spawnArea;
    private final GameModel gameModel;
    private Map<Integer, Point> spawnRequests = new HashMap<>();

    public NPCEngine(List<Integer> NPCIds, Rect spawnArea, GameModel gameModel) {
        this.NPCIds = NPCIds;
        this.spawnArea = spawnArea;
        this.gameModel = gameModel;
    }

    public void computeNextStep() {
        spawnRequests.clear();
        Set<Integer> players = gameModel.getPlayers().keySet();
        NPCIds.stream().filter(id -> !players.contains(id)).forEach(
                id -> spawnRequests.put(id, spawnArea.getRandomPoint())
        );
    }

    public Map<Integer, Point> getSpawnRequests() {
        return spawnRequests;
    }


}
