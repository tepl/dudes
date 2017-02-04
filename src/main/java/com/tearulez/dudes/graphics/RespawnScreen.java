package com.tearulez.dudes.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.tearulez.dudes.Point;
import com.tearulez.dudes.RespawnControls;

public class RespawnScreen extends ScreenAdapter {
    private static final float VIEWPORT_HEIGHT = 1;
    private static final float RESPAWN_CIRCLE_RADIUS = 0.01f;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final OrthographicCamera cam = new OrthographicCamera();
    private final BitmapFont font = new BitmapFont();
    private final Batch batch = new SpriteBatch();
    private final WorldRenderer worldRenderer;
    private final RespawnControls respawnControls;
    private int mouseX;
    private int mouseY;

    public RespawnScreen(WorldRenderer worldRenderer, RespawnControls respawnControls) {
        this.worldRenderer = worldRenderer;
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
                Point point = worldRenderer.convertScreenToWorld(screenX, screenY);
                respawnControls.respawnAt(point);
                return true;
            }
        });

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
        renderText();
        renderRespawnPoint();
    }

    private void renderText() {
        worldRenderer.render();
        batch.begin();
        font.setColor(Color.BLACK);
        font.draw(batch, "Click to respawn", 100, 150);
        batch.end();
    }

    private void renderRespawnPoint() {
        Vector3 p = cam.unproject(new Vector3(mouseX, mouseY, 0));
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(p.x, p.y, RESPAWN_CIRCLE_RADIUS, 8);
        shapeRenderer.end();
    }

}
