package com.tearulez.dudes.client;

public interface PlayerControls {
    void movePlayer(float dx, float dy);

    void rotatePlayer(float angle);

    void shootAt(float x, float y);

    void reload();
}
