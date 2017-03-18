package com.tearulez.dudes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tearulez.dudes.Point;
import com.tearulez.dudes.RespawnControls;

public class RespawnScreen extends ScreenAdapter {
    private static final float VIEWPORT_HEIGHT = 1;
    private static final float RESPAWN_CIRCLE_RADIUS = 0.01f;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Viewport viewport;
    private final WorldPresentation worldPresentation;
    private final RespawnControls respawnControls;
    private int mouseX;
    private int mouseY;

    public RespawnScreen(ViewportFactory viewportFactory, WorldPresentation worldPresentation, RespawnControls respawnControls) {
        viewport = viewportFactory.createViewport(VIEWPORT_HEIGHT);
        this.worldPresentation = worldPresentation;
        this.respawnControls = respawnControls;
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
        renderText();
        renderRespawnPoint();
    }

    private void renderText() {
        worldPresentation.render();
    }

    private void renderRespawnPoint() {
        Vector3 p = viewport.unproject(new Vector3(mouseX, mouseY, 0));
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(p.x, p.y, RESPAWN_CIRCLE_RADIUS, 8);
        shapeRenderer.end();
    }

}
