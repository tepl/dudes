package com.tearulez.dudes.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
    private static final float FONT_SCREEN_HEIGHT_FRACTION = 1 / 10f;
    private static final float BUTTON_WIDTH = 300f;
    private static final float BUTTON_HEIGHT = 100f;
    private final WorldRenderer worldRenderer;
    private final Stage stage;
    private final Texture buttonTexture = new Texture(Gdx.files.internal("images/button.png"));
    private final NinePatchDrawable buttonDrawable = new NinePatchDrawable(
            new NinePatch(buttonTexture, 12, 12, 12, 12)
    );
    private final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("fonts/Oswald-Regular.ttf")
    );
    private BitmapFont buttonTextFont = new BitmapFont();

    public MenuScreen(ViewportFactory viewportFactory, List<MenuItem> menuItems, WorldRenderer worldRenderer) {
        stage = new Stage(viewportFactory.createViewport(VIEWPORT_HEIGHT));
        this.worldRenderer = worldRenderer;

        float buttonSpace = stage.getViewport().getWorldHeight() / menuItems.size();
        float x = (stage.getViewport().getWorldWidth() - BUTTON_WIDTH) / 2;
        float y = VIEWPORT_HEIGHT - buttonSpace + (buttonSpace - BUTTON_HEIGHT) / 2;
        TextButtonStyle style = new TextButtonStyle(null, null, null, buttonTextFont);
        for (MenuItem menuItem : menuItems) {
            TextButton button = new TextButton(menuItem.getName(), style);
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
        resizeFont(height);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = buttonTextFont;
        style.fontColor = Color.BLACK;
        style.up = buttonDrawable;
        for (Actor actor : stage.getActors()) {
            ((TextButton) actor).setStyle(style);
        }
        worldRenderer.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    private void resizeFont(int height) {
        buttonTextFont.dispose();
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = (int) (FONT_SCREEN_HEIGHT_FRACTION * height);
        buttonTextFont = generator.generateFont(parameter);
        buttonTextFont.getData().setScale(stage.getViewport().getWorldHeight() / height);
    }

    @Override
    public void render(float delta) {
        worldRenderer.render();
        stage.draw();
    }

    @Override
    public void dispose() {
        buttonTextFont.dispose();
        buttonTexture.dispose();
        stage.dispose();
        generator.dispose();
    }
}
