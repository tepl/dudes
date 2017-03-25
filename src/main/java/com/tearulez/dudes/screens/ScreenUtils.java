package com.tearulez.dudes.screens;

import com.badlogic.gdx.Gdx;

class ScreenUtils {
    static boolean isOneOfKeysPressed(int... keys) {
        for (int key : keys) {
            if (Gdx.input.isKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }
}
