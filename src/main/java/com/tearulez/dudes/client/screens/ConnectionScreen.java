package com.tearulez.dudes.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

import static com.tearulez.dudes.client.screens.ScreenUtils.*;

public class ConnectionScreen extends ScreenAdapter {
    private static final float VIEWPORT_HEIGHT = 480;
    private static final float BUTTON_WIDTH = 400f;
    private static final float BUTTON_HEIGHT = 100f;

    private final Stage stage;

    public ConnectionScreen(ViewportFactory viewportFactory, List<MenuItem> menuItems) {
        Viewport viewport = viewportFactory.createViewport(VIEWPORT_HEIGHT);
        stage = new Stage(viewport);

        // Sizes
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        float titleSpace = h / 10;
        float buttonSpace = (h - titleSpace) / (menuItems.size() + 1);

        // Add title
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = new BitmapFont();
        Label title = new Label("Connection settings", labelStyle);
        title.setBounds(0, h - titleSpace, w, titleSpace);
        stage.addActor(title);

        // Add server text field
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.fontColor = Color.CHARTREUSE;
        style.font = new BitmapFont();
        TextField serverTextField = new TextField("Server-IP-Placeholder", style);
        float x = (w - BUTTON_WIDTH) / 2;
        float y = h - titleSpace - buttonSpace + (buttonSpace - BUTTON_HEIGHT) / 2;
        serverTextField.setBounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        stage.addActor(serverTextField);
        y -= buttonSpace;

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
        float viewportWidth = stage.getViewport().getWorldWidth();
        float viewportHeight = stage.getViewport().getWorldHeight();
        for (Actor actor : stage.getActors()) {
            if (actor instanceof TextButton) {
                TextButton button = (TextButton) actor;
                resizeButton(button, viewportHeight, height);
                button.setX((viewportWidth - BUTTON_WIDTH) / 2);
            } else if (actor instanceof Label) {
                Label label = (Label) actor;
                Label.LabelStyle style = label.getStyle();
                style.font.dispose();
                style.font = scalableFontGenerator.generateFont(viewportHeight, height);
                label.setStyle(style);
            } else if (actor instanceof TextField) {
                TextField textField = (TextField) actor;
                TextField.TextFieldStyle style = textField.getStyle();
                style.font.dispose();
                style.font = scalableFontGenerator.generateFont(viewportHeight, height);
                textField.setStyle(style);
                textField.setX((viewportWidth - BUTTON_WIDTH) / 2);
            }
        }
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.3f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void dispose() {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof TextButton) {
                ((TextButton) actor).getStyle().font.dispose();
            } else if (actor instanceof TextField) {
                ((TextField) actor).getStyle().font.dispose();
            } else if (actor instanceof Label) {
                ((Label) actor).getStyle().font.dispose();
            }
        }
        stage.dispose();
    }
}
