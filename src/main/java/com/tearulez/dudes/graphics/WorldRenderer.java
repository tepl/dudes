package com.tearulez.dudes.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tearulez.dudes.GameState;
import com.tearulez.dudes.Point;
import com.tearulez.dudes.StateSnapshot;
import com.tearulez.dudes.model.GameModel;
import com.tearulez.dudes.model.Player;
import com.tearulez.dudes.model.Wall;

import java.util.List;
import java.util.Optional;

public class WorldRenderer {
    private static final float VIEWPORT_HEIGHT = 50;
    private static final int NUMBER_OF_CIRCLE_SEGMENTS = 8;
    private final GameState state;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Viewport viewport;

    public WorldRenderer(ViewportFactory viewportFactory, GameState state) {
        viewport = viewportFactory.createViewport(VIEWPORT_HEIGHT);
        this.state = state;
    }

    void resize(int width, int height) {
        viewport.update(width, height);
    }

    void render() {
        StateSnapshot stateSnapshot = state.snapshot();
        Camera cam = viewport.getCamera();
        if (stateSnapshot.getPlayer().isPresent()) {
            Point playerPosition = stateSnapshot.getPlayer().get().getPosition();
            cam.position.set(playerPosition.x, playerPosition.y, 0);
        } else {
            cam.position.set(0, 0, 0);
        }
        cam.update();
        shapeRenderer.setProjectionMatrix(cam.combined);

        renderBackground();
        renderWalls(stateSnapshot.getWalls());
        renderPlayers(stateSnapshot.getPlayer(), stateSnapshot.getOtherPlayers());
        renderBullets(stateSnapshot.getBullets());
    }

    Point convertScreenToWorld(int screenX, int screenY) {
        Vector3 p = viewport.unproject(new Vector3(screenX, screenY, 0));
        return Point.create(p.x, p.y);
    }

    private void renderBackground() {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void renderBullets(List<Point> bullets) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Point bullet : bullets) {
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(bullet.x, bullet.y, GameModel.BULLET_CIRCLE_RADIUS, NUMBER_OF_CIRCLE_SEGMENTS);
        }
        shapeRenderer.end();
    }

    private void renderPlayers(Optional<Player> playerOpt, List<Player> otherPlayers) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Player otherPlayer : otherPlayers) {
            renderPlayer(otherPlayer, Color.RED);
        }
        playerOpt.ifPresent(player -> renderPlayer(player, Color.GREEN));
        shapeRenderer.end();
    }

    private void renderPlayer(Player player, Color color) {
        Point position = player.getPosition();

        float healthFactor = 1 - (float) player.getHealth() / Player.MAX_HEALTH;

        shapeRenderer.setColor(color);
        shapeRenderer.circle(position.x, position.y, GameModel.PLAYER_CIRCLE_RADIUS, NUMBER_OF_CIRCLE_SEGMENTS);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(position.x, position.y, GameModel.PLAYER_CIRCLE_RADIUS * healthFactor, NUMBER_OF_CIRCLE_SEGMENTS);
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
                vertices[i * 2] = position.x + point.x;
                vertices[i * 2 + 1] = position.y + point.y;
            }
            shapeRenderer.polygon(vertices);
        }
        shapeRenderer.end();
    }
}
