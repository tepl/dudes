package com.tearulez.dudes;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;
import java.util.Map;

class GameScreen extends ScreenAdapter {
    private final int playerId;
    private final PlayerControls playerControls;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private int width = 0;
    private int height = 0;
    private int mouseX;
    private int mouseY;
    private final int scaleFactor;
    private final GameState state;

    GameScreen(int playerId, PlayerControls playerControls, GameState state) {
        this.playerId = playerId;
        this.playerControls = playerControls;
        this.state = state;
        scaleFactor = 10;
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                mouseX = screenX;
                mouseY = screenY;
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float x = (screenX - width / 2) / scaleFactor;
                float y = -(screenY - height / 2) / scaleFactor;
                playerControls.shootAt(x, y);
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
    public void render(float delta) {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int dx = 0;
        int dy = 0;
        if (isOneOfKeysPressed(Input.Keys.LEFT, Input.Keys.A)) dx -= 1;
        if (isOneOfKeysPressed(Input.Keys.RIGHT, Input.Keys.D)) dx += 1;
        if (isOneOfKeysPressed(Input.Keys.UP, Input.Keys.W)) dy += 1;
        if (isOneOfKeysPressed(Input.Keys.DOWN, Input.Keys.S)) dy -= 1;
        if (dx != 0 || dy != 0) {
            playerControls.movePlayer(dx, dy);
        }

        StateSnapshot stateSnapshot = state.snapshot();

        renderWalls(stateSnapshot.getWalls());
        renderPlayers(stateSnapshot.getPlayers());
        renderBullets(stateSnapshot.getBullets());
        renderCrosshairs();
    }

    private boolean isOneOfKeysPressed(int... keys) {
        for (int key : keys) {
            if (Gdx.input.isKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }

    private void renderBullets(List<Point> bullets) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Point bullet : bullets) {
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(
                    width / 2 + bullet.x * scaleFactor,
                    height / 2 + bullet.y * scaleFactor,
                    GameModel.BULLER_CIRCLE_RADIUS * scaleFactor
            );
        }
        shapeRenderer.end();
    }

    private void renderPlayers(Map<Integer, Player> players) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Map.Entry<Integer, Player> positionEntry : players.entrySet()) {
            int characterId = positionEntry.getKey();
            Player player = positionEntry.getValue();
            Point position = player.getPosition();

            float screenX = width / 2 + position.x * scaleFactor;
            float screenY = height / 2 + position.y * scaleFactor;
            float radius = GameModel.PLAYER_CIRCLE_RADIUS * scaleFactor;
            float healthFactor = 1 - (float) player.getHealth() / Player.MAX_HEALTH;

            if (characterId == playerId) {
                shapeRenderer.setColor(Color.GREEN);
            } else {
                shapeRenderer.setColor(Color.RED);
            }
            shapeRenderer.circle(screenX, screenY, radius);
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(screenX, screenY, radius * healthFactor);
        }
        shapeRenderer.end();
    }

    private void renderWalls(List<Wall> walls) {
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
