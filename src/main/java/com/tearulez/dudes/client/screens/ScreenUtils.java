package com.tearulez.dudes.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class ScreenUtils {
    private static final float FONT_TO_SCREEN_HEIGHT_RATIO = 1 / 10f;
    private static Texture buttonTexture = new Texture(Gdx.files.internal("res/images/button.png"));
    public static ScalableFontGenerator scalableFontGenerator = new ScalableFontGenerator(
            "res/fonts/Oswald-Regular.ttf",
            FONT_TO_SCREEN_HEIGHT_RATIO
    );

    static boolean isOneOfKeysPressed(int... keys) {
        for (int key : keys) {
            if (Gdx.input.isKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }

    static TextButton createButton(String text, Runnable callback) {
        NinePatchDrawable drawable = new NinePatchDrawable(new NinePatch(buttonTexture, 12, 12, 12, 12));
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(drawable, null, null, new BitmapFont());
        buttonStyle.fontColor = Color.BLACK;
        TextButton button = new TextButton(text, buttonStyle);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.run();
            }
        });
        return button;
    }

    static void resizeButton(TextButton button, float viewportHeight, int screenHeight) {
        TextButton.TextButtonStyle style = button.getStyle();
        style.font.dispose();
        style.font = scalableFontGenerator.generateFont(viewportHeight, screenHeight);
        button.setStyle(style);
    }

    public static void dispose() {
        buttonTexture.dispose();
        scalableFontGenerator.dispose();
    }
}
