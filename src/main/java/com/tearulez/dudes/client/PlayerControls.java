package com.tearulez.dudes.client;

public interface PlayerControls {
    void movePlayer(float dx, float dy);

    void shootAt(float x, float y);

    void reload();
}
