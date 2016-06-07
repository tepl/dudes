package com.tearulez.dudes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Map;

public class GameAdapter extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private GameClient gameClient;
    private int width = 0;
    private int height = 0;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        gameClient = new GameClient();
        gameClient.init();
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int dx = 0;
        int dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) dy -= 1;
        if (dx != 0 || dy != 0) {
            gameClient.movePlayer(dx * 0.01f, dy * 0.01f);
        }


        if (gameClient.isInitialized()) {
            int playerId = gameClient.getPlayerId();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (Map.Entry<Integer, Position> positionEntry : gameClient.getPositions().entrySet()) {
                Position p = positionEntry.getValue();
                int characterId = positionEntry.getKey();

                if (characterId == playerId) {
                    shapeRenderer.setColor(Color.GREEN);
                } else {
                    shapeRenderer.setColor(Color.RED);
                }
                shapeRenderer.circle(width / 2 + p.x * 100, height / 2 + p.y * 100, 10);
            }
            shapeRenderer.end();
        }

    }
}
