package com.tearulez.dudes.graphics;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;


public class MenuScreen extends ScreenAdapter {
    private static final float VIEWPORT_HEIGHT = 1000;
    private static final float BUTTON_WIDTH = 500f;
    private static final float BUTTON_HEIGHT = 200f;
    private final Runnable resumeCallback;
    private final Runnable exitCallback;
    private final WorldRenderer worldRenderer;
    private final OrthographicCamera cam = new OrthographicCamera();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Rectangle resumeButtonRect;
    private Rectangle exitButtonRect;
    private final BitmapFont font = new BitmapFont();
    private final Batch batch = new SpriteBatch();

    public MenuScreen(Runnable resumeCallback, Runnable exitCallback, WorldRenderer worldRenderer) {
        this.resumeCallback = resumeCallback;
        this.exitCallback = exitCallback;
        this.worldRenderer = worldRenderer;
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 p3 = cam.unproject(new Vector3(screenX, screenY, 0));
                Vector2 p2 = new Vector2(p3.x, p3.y);
                if (resumeButtonRect.contains(p2)) {
                    resumeCallback.run();
                }
                if (exitButtonRect.contains(p2)) {
                    exitCallback.run();
                }
                return true;
            }
        });
    }

    @Override
    public void hide() {
        Gdx.input.setCursorCatched(true);
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int width, int height) {
        worldRenderer.resize(width, height);
        cam.viewportWidth = VIEWPORT_HEIGHT * width / height;
        cam.viewportHeight = VIEWPORT_HEIGHT;
        cam.update();

        resumeButtonRect = new Rectangle(-BUTTON_WIDTH / 2, 300 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        exitButtonRect = new Rectangle(-BUTTON_WIDTH / 2, -300 - BUTTON_HEIGHT / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void render(float delta) {
        worldRenderer.render();
        renderButtons();
    }

    private void renderButtons() {
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(resumeButtonRect.x, resumeButtonRect.y, resumeButtonRect.width, resumeButtonRect.height);
        shapeRenderer.rect(exitButtonRect.x, exitButtonRect.y, exitButtonRect.width, exitButtonRect.height);
        shapeRenderer.end();

        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        font.setColor(Color.GREEN);
        font.getData().setScale(10f);
        font.draw(batch, "Resume", resumeButtonRect.x, resumeButtonRect.y + resumeButtonRect.height / 3 * 2);
        font.draw(batch, "Exit", exitButtonRect.x, exitButtonRect.y + exitButtonRect.height / 3 * 2);
        batch.end();
    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
    }
}
