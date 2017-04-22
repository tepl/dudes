package com.tearulez.dudes.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String MIN_SPAWN_DISTANCE = "minSpawnDistance";
    private Properties properties;

    public Config() {
        try (FileInputStream fileInput = new FileInputStream(new File("config"))) {
            properties = new Properties();
            properties.load(fileInput);
            fileInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    float getMinSpawnDistance() {
        return Float.valueOf(properties.getProperty(MIN_SPAWN_DISTANCE));
    }
}