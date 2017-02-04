package com.tearulez.dudes.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.tearulez.dudes.*;

import java.util.List;
import java.util.Map;

public class WorldRenderer {
    private final int playerId;
    private final GameState state;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final float scaleFactor;
    private int width;
    private int height;

    public WorldRenderer(int playerId, GameState state, float scaleFactor) {
        this.playerId = playerId;
        this.state = state;
        this.scaleFactor = scaleFactor;
    }

    void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    void render() {
        renderBackground();
        StateSnapshot stateSnapshot = state.snapshot();
        renderWalls(stateSnapshot.getWalls());
        renderPlayers(stateSnapshot.getPlayers());
        renderBullets(stateSnapshot.getBullets());
    }

    Point convertScreenToWorld(int screenX, int screenY) {
        float x = (screenX - width / 2) / scaleFactor;
        float y = (screenY - height / 2) / scaleFactor;
        return Point.create(x, y);
    }

    private void renderBackground() {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void renderBullets(List<Point> bullets) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Point bullet : bullets) {
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(
                    width / 2 + bullet.x * scaleFactor,
                    height / 2 + bullet.y * scaleFactor,
                    GameModel.BULLET_CIRCLE_RADIUS * scaleFactor
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
}
