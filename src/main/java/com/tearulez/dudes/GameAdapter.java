package com.tearulez.dudes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameAdapter extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(50, 50, 32);
        shapeRenderer.end();

    }
}
