package com.tearulez.dudes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameAdapter extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private int x = 0, y = 0;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) x += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) y -= 1;

        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(400 + x, 240 + y, 32);
        shapeRenderer.end();

    }
}
