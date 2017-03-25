package com.tearulez.dudes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tearulez.dudes.Point;
import com.tearulez.dudes.RespawnControls;

import static com.tearulez.dudes.screens.ScreenUtils.isOneOfKeysPressed;

public class RespawnScreen extends ScreenAdapter {
    private static final float VIEWPORT_HEIGHT = 1;
    private static final float RESPAWN_CIRCLE_RADIUS = 0.01f;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Viewport viewport;
    private final WorldPresentation worldPresentation;
    private final RespawnControls respawnControls;

    public RespawnScreen(ViewportFactory viewportFactory, WorldPresentation worldPresentation, RespawnControls respawnControls) {
        viewport = viewportFactory.createViewport(VIEWPORT_HEIGHT);
        this.worldPresentation = worldPresentation;
        this.respawnControls = respawnControls;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Point point = worldPresentation.convertScreenToWorld(screenX, screenY);
                respawnControls.respawnAt(point);
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
        worldPresentation.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        keyboardPanning();
        mousePanning();
        renderText();
        renderRespawnPoint();
    }

    private void keyboardPanning() {
        float dx = 0;
        float dy = 0;
        if (isOneOfKeysPressed(Input.Keys.LEFT, Input.Keys.A)) dx -= 1;
        if (isOneOfKeysPressed(Input.Keys.RIGHT, Input.Keys.D)) dx += 1;
        if (isOneOfKeysPressed(Input.Keys.UP, Input.Keys.W)) dy += 1;
        if (isOneOfKeysPressed(Input.Keys.DOWN, Input.Keys.S)) dy -= 1;
        if (dx != 0 || dy != 0) {
            worldPresentation.translate(dx, dy);
        }
    }

    private void mousePanning() {
        float dx = 0;
        float dy = 0;
        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        if (x == Gdx.graphics.getWidth() - 1) dx = 1;
        if (x == 0) dx = -1;
        if (y == Gdx.graphics.getHeight() - 1) dy = -1;
        if (y == 0) dy = 1;
        if (dx != 0 || dy != 0) {
            worldPresentation.translate(dx, dy);
        }
    }

    private void renderText() {
        worldPresentation.render();
    }

    private void renderRespawnPoint() {
        Vector3 p = viewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(p.x, p.y, RESPAWN_CIRCLE_RADIUS, 8);
        shapeRenderer.end();
    }

}
