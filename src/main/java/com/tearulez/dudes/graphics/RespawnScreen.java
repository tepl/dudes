package com.tearulez.dudes.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RespawnScreen extends ScreenAdapter {
    private final BitmapFont font = new BitmapFont();
    private final Batch batch = new SpriteBatch();
    private final Runnable callback;

    public RespawnScreen(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "Click to respawn", 100, 150);
        batch.end();

        if (Gdx.input.isTouched()) {
            callback.run();
            dispose();
        }
    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
    }
}
