package com.tearulez.dudes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;
import java.util.Map;

class GameAdapter extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private final GameClient gameClient;
    private int width = 0;
    private int height = 0;
    private int mouseX;
    private int mouseY;

    GameAdapter(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                mouseX = screenX;
                mouseY = screenY;
                return true;
            }
        });
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
            List<Wall> walls = state.getWalls();
            Map<Integer, Point> playerPositions = state.getPositions();

            renderWalls(scaleFactor, walls);
            renderPlayers(scaleFactor, playerPositions);
            renderCrosshairs();
        }

    }

    private void renderPlayers(int scaleFactor, Map<Integer, Point> playerPositions) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int playerId = gameClient.getPlayerId();
        for (Map.Entry<Integer, Point> positionEntry : playerPositions.entrySet()) {
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

    private void renderWalls(int scaleFactor, List<Wall> walls) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLUE);
        for (Wall wall : walls) {
            int size = wall.getPoints().size();
            Point position = wall.getPosition();
            float[] vertices = new float[size * 2];
            for (int i = 0; i < size; i++) {
                Point point = wall.getPoints().get(i);
                vertices[i * 2] = width / 2 + (position.x + point.x) * scaleFactor;
                vertices[i * 2 + 1] = height / 2 + (position.y + point.y) * scaleFactor;
            }
            shapeRenderer.polygon(vertices);
        }
        shapeRenderer.end();
    }

    private void renderCrosshairs() {
        int crosshairsSize = 4;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.line(mouseX - crosshairsSize, height - mouseY, mouseX + crosshairsSize, height - mouseY);
        shapeRenderer.line(mouseX, height - mouseY - crosshairsSize, mouseX, height - mouseY + crosshairsSize);
        shapeRenderer.end();
    }
}
