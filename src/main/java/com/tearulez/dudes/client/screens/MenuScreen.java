package com.tearulez.dudes.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import java.util.List;


public class MenuScreen extends ScreenAdapter {
    private static final float VIEWPORT_HEIGHT = 480;
    private static final float FONT_TO_SCREEN_HEIGHT_RATIO = 1 / 10f;
    private static final float BUTTON_WIDTH = 300f;
    private static final float BUTTON_HEIGHT = 100f;
    private final WorldPresentation worldPresentation;
    private final Stage stage;
    private final Texture buttonTexture = new Texture(Gdx.files.internal("images/button.png"));
    private ScalableFontGenerator scalableFontGenerator = new ScalableFontGenerator(
            "fonts/Oswald-Regular.ttf",
            FONT_TO_SCREEN_HEIGHT_RATIO
    );

    public MenuScreen(ViewportFactory viewportFactory, List<MenuItem> menuItems, WorldPresentation worldPresentation) {
        stage = new Stage(viewportFactory.createViewport(VIEWPORT_HEIGHT));
        this.worldPresentation = worldPresentation;

        float buttonSpace = stage.getViewport().getWorldHeight() / menuItems.size();
        float x = (stage.getViewport().getWorldWidth() - BUTTON_WIDTH) / 2;
        float y = VIEWPORT_HEIGHT - buttonSpace + (buttonSpace - BUTTON_HEIGHT) / 2;

        for (MenuItem menuItem : menuItems) {
            TextButtonStyle buttonStyle = new TextButtonStyle(
                    new NinePatchDrawable(new NinePatch(buttonTexture, 12, 12, 12, 12)),
                    null,
                    null,
                    new BitmapFont());
            buttonStyle.fontColor = Color.BLACK;
            TextButton button = new TextButton(menuItem.getName(), buttonStyle);

            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    menuItem.getCallback().run();
                }
            });
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
        for (Actor actor : stage.getActors()) {
            TextButton.TextButtonStyle style = ((TextButton) actor).getStyle();
            style.font.dispose();
            style.font = scalableFontGenerator.generateFont(stage.getViewport().getWorldHeight(), height);
            ((TextButton) actor).setStyle(style);
        }
        worldPresentation.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        worldPresentation.render();
        stage.draw();
    }

    @Override
    public void dispose() {
        scalableFontGenerator.dispose();
        for (Actor actor : stage.getActors()) {
            ((TextButton) actor).getStyle().font.dispose();
        }
        buttonTexture.dispose();
        stage.dispose();
    }
}
