package com.tearulez.dudes.client;

import com.badlogic.gdx.Gdx;

public class ClippedMouse {
    public static class MousePosition {
        public final int x;
        public final int y;

        private MousePosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static MousePosition clipAndGetPosition() {
        int x = Math.min(Gdx.graphics.getWidth() - 1, Math.max(Gdx.input.getX(), 0));
        int y = Math.min(Gdx.graphics.getHeight() - 1, Math.max(Gdx.input.getY(), 0));
        Gdx.input.setCursorPosition(x, y);
        return new MousePosition(x, y);
    }
}
