package com.tearulez.dudes.graphics;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.tearulez.dudes.*;


public class GameScreen extends ScreenAdapter {
    private static final float VIEWPORT_HEIGHT = 1;
    private static final float CROSSHAIR_SIZE = 0.02f;
    private final PlayerControls playerControls;
    private final WorldRenderer worldRenderer;
    private final OrthographicCamera cam = new OrthographicCamera();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private int mouseX;
    private int mouseY;

    public GameScreen(PlayerControls playerControls, WorldRenderer worldRenderer) {
        this.playerControls = playerControls;
        this.worldRenderer = worldRenderer;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                mouseX = screenX;
                mouseY = screenY;
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Point target = worldRenderer.convertScreenToWorld(screenX, screenY);
                playerControls.shootAt(target.x, target.y);
                return true;
            }
        });
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int width, int height) {
        worldRenderer.resize(width, height);
        cam.viewportWidth = VIEWPORT_HEIGHT * width / height;
        cam.viewportHeight = VIEWPORT_HEIGHT;
        cam.update();
    }

    @Override
    public void render(float delta) {
        if (isOneOfKeysPressed(Input.Keys.ESCAPE)) System.exit(0);
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
        Vector3 p = cam.unproject(new Vector3(mouseX, mouseY, 0));
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.line(p.x - CROSSHAIR_SIZE, p.y, p.x + CROSSHAIR_SIZE, p.y);
        shapeRenderer.line(p.x, p.y - CROSSHAIR_SIZE, p.x, p.y + CROSSHAIR_SIZE);
        shapeRenderer.end();
    }
}
