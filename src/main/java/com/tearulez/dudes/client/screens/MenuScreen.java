package com.tearulez.dudes.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import java.util.List;

import static com.tearulez.dudes.client.screens.ScreenUtils.createButton;
import static com.tearulez.dudes.client.screens.ScreenUtils.resizeFont;


public class MenuScreen extends ScreenAdapter {
    private static final float VIEWPORT_HEIGHT = 480;
    private static final float BUTTON_WIDTH = 400f;
    private static final float BUTTON_HEIGHT = 100f;
    private final WorldPresentation worldPresentation;
    private final Stage stage;


    public MenuScreen(ViewportFactory viewportFactory, List<MenuItem> menuItems, WorldPresentation worldPresentation) {
        stage = new Stage(viewportFactory.createViewport(VIEWPORT_HEIGHT));
        this.worldPresentation = worldPresentation;

        // Sizes
        float buttonSpace = stage.getViewport().getWorldHeight() / menuItems.size();
        float x = (stage.getViewport().getWorldWidth() - BUTTON_WIDTH) / 2;
        float y = VIEWPORT_HEIGHT - buttonSpace + (buttonSpace - BUTTON_HEIGHT) / 2;

        // Add buttons
        for (MenuItem menuItem : menuItems) {
            TextButton button = createButton(menuItem.getName(), menuItem.getCallback());
            button.setBounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
            stage.addActor(button);
            y -= buttonSpace;
        }
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setCursorCatched(true);
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int width, int height) {

        // Get viewport dimensions
        float viewportWidth = stage.getViewport().getWorldWidth();
        float viewportHeight = stage.getViewport().getWorldHeight();

        // Resize font
        resizeFont(stage, viewportHeight, height);

        // Reposition actors
        for (Actor actor : stage.getActors()) {
            actor.setX((viewportWidth - BUTTON_WIDTH) / 2);
        }

        // Update world presentation size
        if (worldPresentation != null) worldPresentation.resize(width, height);

        // Update viewport size
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        if (worldPresentation == null) {
            Gdx.gl.glClearColor(0.1f, 0.3f, 0.1f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        } else {
            worldPresentation.render();
        }
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
