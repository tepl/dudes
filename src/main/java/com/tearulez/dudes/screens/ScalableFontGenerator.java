package com.tearulez.dudes.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

class ScalableFontGenerator {
    private final FreeTypeFontGenerator generator;
    private float fontToScreenHeightRatio;

    ScalableFontGenerator(String fontFilePath, float fontToScreenHeightRatio) {
        generator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilePath));
        this.fontToScreenHeightRatio = fontToScreenHeightRatio;
    }

    BitmapFont generateFont(float viewportHeight, int screenHeight) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = (int) (fontToScreenHeightRatio * screenHeight);
        BitmapFont font = generator.generateFont(parameter);
        font.getData().setScale(viewportHeight / screenHeight);
        return font;
    }

    void dispose() {
        generator.dispose();
    }
}
