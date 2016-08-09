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
        System.out.println("Game Client is initialized");
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
            gameClient.movePlayer(dx, dy);
        }

        if (gameClient.isInitialized()) {
            int scaleFactor = 10;

            GameState state = gameClient.getState();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.BLUE);
            for (Wall wall : state.getWalls()) {
                int size = wall.getPoints().size();
                float[] vertices = new float[size * 2];
                for (int i = 0; i < size; i++) {
                    Point point = wall.getPoints().get(i);
                    vertices[i * 2] = width / 2 + point.x * scaleFactor;
                    vertices[i * 2 + 1] = height / 2 + point.y * scaleFactor;
                }
                shapeRenderer.polygon(vertices);
            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            int playerId = gameClient.getPlayerId();
            for (Map.Entry<Integer, Point> positionEntry : state.getPositions().entrySet()) {
                Point p = positionEntry.getValue();
                int characterId = positionEntry.getKey();


                if (characterId == playerId) {
                    shapeRenderer.setColor(Color.GREEN);
                } else {
                    shapeRenderer.setColor(Color.RED);
                }
                shapeRenderer.circle(
                        width / 2 + p.x * scaleFactor,
                        height / 2 + p.y * scaleFactor,
                        GameModel.PLAYER_CIRCLE_SIZE * scaleFactor
                );
            }
            shapeRenderer.end();

        }

    }
}
