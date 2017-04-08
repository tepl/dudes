package com.tearulez.dudes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCEngine {
    private List<Integer> NPCIds;
    private final Rect spawnArea;
    private Map<Integer, Point> spawnRequests = new HashMap<>();

    public NPCEngine(List<Integer> NPCIds, Rect spawnArea) {
        this.NPCIds = NPCIds;
        this.spawnArea = spawnArea;
    }

    public void computeNextStep() {
        spawnRequests.clear();
        for (Integer id : NPCIds) {
            spawnRequests.put(id, spawnArea.getRandomPoint());
        }
    }

    public Map<Integer, Point> getSpawnRequests() {
        return spawnRequests;
    }


}
