package com.tearulez.dudes.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ScreenUtils {
    private static final float FONT_TO_SCREEN_HEIGHT_RATIO = 1 / 10f;
    private static final Skin skin = new Skin(Gdx.files.internal("res/skin/uiskin.json"));
    private static ScalableFontGenerator scalableFontGenerator = new ScalableFontGenerator(
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

    static void resizeFont(Stage stage, float viewportHeight, int screenHeight) {
        String fontName = "default-font";
        skin.getFont(fontName).dispose();
        skin.add(fontName, scalableFontGenerator.generateFont(viewportHeight, screenHeight));
        for (Actor actor : stage.getActors()) {
            if (actor instanceof TextButton) {
                TextButton button = (TextButton) actor;
                TextButton.TextButtonStyle style = button.getStyle();
                style.font = skin.getFont(fontName);
                button.setStyle(style);
            } else if (actor instanceof Label) {
                Label label = (Label) actor;
                Label.LabelStyle style = label.getStyle();
                style.font = skin.getFont(fontName);
                label.setStyle(style);
            } else if (actor instanceof TextField) {
                TextField textField = (TextField) actor;
                TextField.TextFieldStyle style = textField.getStyle();
                style.font = skin.getFont(fontName);
                textField.setStyle(style);
            }
        }
    }

    static Label createLabel(String text) {
        return new Label(text, skin);
    }

    static TextField createTextField(String text) {
        return new TextField(text, skin);
    }

    static TextButton createButton(String text, Runnable callback) {
        TextButton button = new TextButton(text, skin, "default");
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.run();
            }
        });
        return button;
    }

    public static void dispose() {
        skin.dispose();
        scalableFontGenerator.dispose();
    }
}
