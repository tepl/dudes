package com.tearulez.dudes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MainMenuScreen extends ScreenAdapter {
    private final Game game;
    private final Screen gameScreen;
    private final BitmapFont font = new BitmapFont();
    private final Batch batch = new SpriteBatch();

    MainMenuScreen(Game game, Screen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "Welcome to Dudes!!! ", 100, 150);
        font.draw(batch, "Tap anywhere to begin!", 100, 100);
        batch.end();

        if (Gdx.input.isTouched()) {
            game.setScreen(gameScreen);
            dispose();
        }
    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
    }
}
