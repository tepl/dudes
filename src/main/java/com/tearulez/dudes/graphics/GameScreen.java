package com.tearulez.dudes.graphics;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.tearulez.dudes.*;


public class GameScreen extends ScreenAdapter {
    private final PlayerControls playerControls;
    private final WorldRenderer worldRenderer;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private int height = 0;
    private int mouseX;
    private int mouseY;

    public GameScreen(PlayerControls playerControls, WorldRenderer worldRenderer) {
        this.playerControls = playerControls;
        this.worldRenderer = worldRenderer;
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                mouseX = screenX;
                mouseY = screenY;
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int fixedScreenY = height - screenY;
                Point target = worldRenderer.convertScreenToWorld(screenX, fixedScreenY);
                playerControls.shootAt(target.x, target.y);
                return true;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        this.height = height;
        worldRenderer.resize(width, height);
    }

    @Override
    public void render(float delta) {
        int dx = 0;
        int dy = 0;
        if (isOneOfKeysPressed(Input.Keys.LEFT, Input.Keys.A)) dx -= 1;
        if (isOneOfKeysPressed(Input.Keys.RIGHT, Input.Keys.D)) dx += 1;
        if (isOneOfKeysPressed(Input.Keys.UP, Input.Keys.W)) dy += 1;
        if (isOneOfKeysPressed(Input.Keys.DOWN, Input.Keys.S)) dy -= 1;
        if (dx != 0 || dy != 0) {
            playerControls.movePlayer(dx, dy);
        }
        worldRenderer.render();
        renderCrosshairs();
    }

    private boolean isOneOfKeysPressed(int... keys) {
        for (int key : keys) {
            if (Gdx.input.isKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }

    private void renderCrosshairs() {
        int crosshairsSize = 4;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        int fixedY = height - mouseY;
        shapeRenderer.line(mouseX - crosshairsSize, fixedY, mouseX + crosshairsSize, fixedY);
        shapeRenderer.line(mouseX, height - mouseY - crosshairsSize, mouseX, fixedY + crosshairsSize);
        shapeRenderer.end();
    }
}
