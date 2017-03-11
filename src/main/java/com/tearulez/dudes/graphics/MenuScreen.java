package com.tearulez.dudes.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;


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
    private final TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle(
            null,
            null,
            null,
            buttonTextFont
    );
    private final TextButton resumeButton = new TextButton("Resume", textButtonStyle);
    private final TextButton exitButton = new TextButton("Exit", textButtonStyle);


    public MenuScreen(ViewportFactory viewportFactory, Runnable resumeCallback, Runnable exitCallback, WorldRenderer worldRenderer) {
        stage = new Stage(viewportFactory.createViewport(VIEWPORT_HEIGHT));
        this.worldRenderer = worldRenderer;
        stage.addActor(resumeButton);
        stage.addActor(exitButton);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resumeCallback.run();
            }
        });
        resumeButton.setBounds(
                (stage.getViewport().getWorldWidth() - BUTTON_WIDTH) / 2,
                stage.getViewport().getWorldHeight() * 3 / 4 - BUTTON_HEIGHT / 2,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        );

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                exitCallback.run();
            }
        });
        exitButton.setBounds(
                (stage.getViewport().getWorldWidth() - BUTTON_WIDTH) / 2,
                stage.getViewport().getWorldHeight() / 4 - BUTTON_HEIGHT / 2,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        );
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
        resumeButton.setStyle(style);
        exitButton.setStyle(style);

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
