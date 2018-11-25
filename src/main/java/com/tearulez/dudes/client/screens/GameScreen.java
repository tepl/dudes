package com.tearulez.dudes.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tearulez.dudes.client.ClippedMouse;
import com.tearulez.dudes.client.PlayerControls;
import com.tearulez.dudes.common.snapshot.Point;

import static com.tearulez.dudes.client.screens.ScreenUtils.isOneOfKeysPressed;


public class GameScreen extends ScreenAdapter {
    private static final float VIEWPORT_HEIGHT = 1;
    private static final float CROSSHAIR_SIZE = 0.02f;
    private final PlayerControls playerControls;
    private final Runnable escapeCallback;
    private final WorldPresentation worldPresentation;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Viewport viewport;

    public GameScreen(ViewportFactory viewportFactory, PlayerControls playerControls, Runnable escapeCallback, WorldPresentation worldPresentation) {
        viewport = viewportFactory.createViewport(VIEWPORT_HEIGHT);
        this.playerControls = playerControls;
        this.escapeCallback = escapeCallback;
        this.worldPresentation = worldPresentation;
    }

    @Override
    public void resize(int width, int height) {
        worldPresentation.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        float dx = 0;
        float dy = 0;
        if (isOneOfKeysPressed(Input.Keys.LEFT, Input.Keys.A)) dx -= 1;
        if (isOneOfKeysPressed(Input.Keys.RIGHT, Input.Keys.D)) dx += 1;
        if (isOneOfKeysPressed(Input.Keys.UP, Input.Keys.W)) dy += 1;
        if (isOneOfKeysPressed(Input.Keys.DOWN, Input.Keys.S)) dy -= 1;
        if (dx != 0 || dy != 0) {
            playerControls.movePlayer(dx, dy);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            playerControls.reload();
        }
        worldPresentation.render();
        renderCrosshairs();
        if (isOneOfKeysPressed(Input.Keys.ESCAPE)) escapeCallback.run();
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            ClippedMouse.MousePosition p = ClippedMouse.clipAndGetPosition();
            Point target = worldPresentation.convertScreenToWorld(p.x, p.y);
            playerControls.shootAt(target.x, target.y);
        }
    }

    private void renderCrosshairs() {
        ClippedMouse.MousePosition mp = ClippedMouse.clipAndGetPosition();
        Vector3 p = viewport.unproject(new Vector3(mp.x, mp.y, 0));
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.line(p.x - CROSSHAIR_SIZE, p.y, p.x + CROSSHAIR_SIZE, p.y);
        shapeRenderer.line(p.x, p.y - CROSSHAIR_SIZE, p.x, p.y + CROSSHAIR_SIZE);
        shapeRenderer.end();
    }
}
