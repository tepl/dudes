package com.tearulez.dudes.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.tearulez.dudes.common.snapshot.Point;
import com.tearulez.dudes.client.SpawnControls;

import static com.tearulez.dudes.client.screens.ScreenUtils.isOneOfKeysPressed;

public class SpawnScreen extends ScreenAdapter {
    private static final float VIEWPORT_HEIGHT = 1000;
    private static final float FONT_TO_SCREEN_HEIGHT_RATIO = 1 / 20f;
    private static final float MOUSE_CURSOR_RADIUS = VIEWPORT_HEIGHT / 100;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Stage stage;
    private final WorldPresentation worldPresentation;
    private final Runnable escapeCallback;
    private final SpawnControls spawnControls;

    private ScalableFontGenerator scalableFontGenerator = new ScalableFontGenerator(
            "fonts/Oswald-Regular.ttf",
            FONT_TO_SCREEN_HEIGHT_RATIO
    );

    public SpawnScreen(ViewportFactory viewportFactory, WorldPresentation worldPresentation, Runnable escapeCallback,
                       SpawnControls spawnControls, String text) {
        stage = new Stage(viewportFactory.createViewport(VIEWPORT_HEIGHT));
        this.worldPresentation = worldPresentation;
        this.escapeCallback = escapeCallback;
        this.spawnControls = spawnControls;
        Label label = new Label(text, new Label.LabelStyle(new BitmapFont(), Color.BLACK));
        label.setPosition(200f, 200f);
        stage.addActor(label);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Point point = worldPresentation.convertScreenToWorld(screenX, screenY);
                spawnControls.spawnAt(point);
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    escapeCallback.run();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int width, int height) {
        for (Actor actor : stage.getActors()) {
            Label.LabelStyle style = ((Label) actor).getStyle();
            style.font.dispose();
            style.font = scalableFontGenerator.generateFont(stage.getViewport().getWorldHeight(), height);
            ((Label) actor).setStyle(style);
        }
        worldPresentation.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        keyboardPanning();
        mousePanning();
        worldPresentation.render();
        stage.draw();
        renderSpawnPoint();
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

    private void renderSpawnPoint() {
        Vector3 p = stage.getViewport().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        shapeRenderer.setProjectionMatrix(stage.getViewport().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(p.x, p.y, MOUSE_CURSOR_RADIUS, 8);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        scalableFontGenerator.dispose();
        for (Actor actor : stage.getActors()) {
            ((Label) actor).getStyle().font.dispose();
        }
        stage.dispose();
    }
}
