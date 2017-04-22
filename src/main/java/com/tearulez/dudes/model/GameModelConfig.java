package com.tearulez.dudes.model;

import java.util.Map;

public class GameModelConfig {
    private static final String MIN_SPAWN_DISTANCE = "minSpawnDistance";
    private static final String BULLET_SPEED = "bulletSpeed";
    private static final String MAGAZINE_SIZE = "magazineSize";
    private final Map<String, String> configData;

    public GameModelConfig(Map<String, String> configData) {
        this.configData = configData;
    }

    float getMinSpawnDistance() {
        return Float.valueOf(configData.get(MIN_SPAWN_DISTANCE));
    }

    float getBulletSpeed() {
        return Float.valueOf(configData.get(BULLET_SPEED));
    }

    int getMagazineSize() {
        return Integer.valueOf(configData.get(MAGAZINE_SIZE));
    }
}