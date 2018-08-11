package com.tearulez.dudes.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tearulez.dudes.client.GameState;
import com.tearulez.dudes.client.SoundSettings;
import com.tearulez.dudes.common.snapshot.Player;
import com.tearulez.dudes.common.snapshot.Point;
import com.tearulez.dudes.common.snapshot.StateSnapshot;
import com.tearulez.dudes.common.snapshot.Wall;

import java.util.List;
import java.util.Optional;

public class WorldPresentation {
    private static final float VIEWPORT_HEIGHT = 50;
    private static final float WORLD_SIZE = 200;
    private static final float NUMBER_OF_TILES = 8;
    private static final int NUMBER_OF_CIRCLE_SEGMENTS = 8;
    private final GameState state;
    private Viewport viewport;

    // Sounds
    private final SoundSettings soundSettings;
    private Sound dryFireSound = Gdx.audio.newSound(Gdx.files.internal("res/sounds/dryfire.mp3"));
    private Sound reloadingSound = Gdx.audio.newSound(Gdx.files.internal("res/sounds/reload.mp3"));
    private Sound shotSound = Gdx.audio.newSound(Gdx.files.internal("res/sounds/M4A1.mp3"));

    // Graphics
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private TextureRegion grass = new TextureRegion(new Texture(Gdx.files.internal("res/images/grass.png")));
    private SpriteBatch spriteBatch = new SpriteBatch();
    private TextureRegion roof = new TextureRegion(new Texture(Gdx.files.internal("res/images/roof.png")));
    private PolygonSpriteBatch polyBatch = new PolygonSpriteBatch();
    private EarClippingTriangulator triangulator;

    public WorldPresentation(ViewportFactory viewportFactory, GameState state, SoundSettings soundSettings) {
        viewport = viewportFactory.createViewport(VIEWPORT_HEIGHT);
        this.state = state;
        this.soundSettings = soundSettings;

        // Graphics
        triangulator = new EarClippingTriangulator();
        grass.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        grass.setRegion(0, 0, NUMBER_OF_TILES, NUMBER_OF_TILES);
        roof.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        roof.setRegion(0, 0, NUMBER_OF_TILES, NUMBER_OF_TILES);
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
        }
        cam.update();
        shapeRenderer.setProjectionMatrix(cam.combined);
        spriteBatch.setProjectionMatrix(cam.combined);
        polyBatch.setProjectionMatrix(cam.combined);

        renderBackground();
        renderWalls(stateSnapshot.getWalls());
        renderPlayers(stateSnapshot.getPlayer(), stateSnapshot.getOtherPlayers(), stateSnapshot.getPlayerRadius());
        renderBullets(stateSnapshot.getBullets(), stateSnapshot.getBulletRadius());
        playSounds(stateSnapshot);
    }

    private void playSounds(StateSnapshot stateSnapshot) {
        if (stateSnapshot.wasDryFire()) {
            playSound(dryFireSound);
        }
        if (stateSnapshot.wasReload()) {
            playSound(reloadingSound);
        }
        if (stateSnapshot.wasShot()) {
            playSound(shotSound);
        }
    }

    private void playSound(Sound sound) {
        sound.play(soundSettings.getVolume());
    }

    Point convertScreenToWorld(int screenX, int screenY) {
        Vector3 p = viewport.unproject(new Vector3(screenX, screenY, 0));
        return Point.create(p.x, p.y);
    }

    private void renderBackground() {
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.begin();
        spriteBatch.draw(grass, -WORLD_SIZE / 2, -WORLD_SIZE / 2, WORLD_SIZE, WORLD_SIZE);
        spriteBatch.end();
    }

    private void renderBullets(List<Point> bullets, float bulletRadius) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Point bullet : bullets) {
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(bullet.x, bullet.y, bulletRadius, NUMBER_OF_CIRCLE_SEGMENTS);
        }
        shapeRenderer.end();
    }

    private void renderPlayers(Optional<Player> playerOpt, List<Player> otherPlayers, float playerRadius) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Player otherPlayer : otherPlayers) {
            renderPlayer(otherPlayer, Color.RED, playerRadius);
        }
        playerOpt.ifPresent(player -> renderPlayer(player, Color.GREEN, playerRadius));
        shapeRenderer.end();
    }

    private void renderPlayer(Player player, Color color, float playerRadius) {
        Point position = player.getPosition();

        float healthFactor = 1 - (float) player.getHealth() / Player.MAX_HEALTH;

        shapeRenderer.setColor(color);
        shapeRenderer.circle(position.x, position.y, playerRadius, NUMBER_OF_CIRCLE_SEGMENTS);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(position.x, position.y, playerRadius * healthFactor, NUMBER_OF_CIRCLE_SEGMENTS);
    }

    private void renderWalls(List<Wall> walls) {
        polyBatch.begin();
        for (Wall wall : walls) {
            int size = wall.getPoints().size();
            Point position = wall.getPosition();
            float[] vertices = new float[size * 2];
            for (int i = 0; i < size; i++) {
                Point point = wall.getPoints().get(i);
                vertices[i * 2] = position.x + point.x;
                vertices[i * 2 + 1] = position.y + point.y;
            }
            PolygonRegion polyReg = new PolygonRegion(roof, vertices, triangulator.computeTriangles(vertices).toArray());
            polyBatch.draw(polyReg, 0, 0);
        }
        polyBatch.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
        shotSound.dispose();
        reloadingSound.dispose();
        dryFireSound.dispose();
    }

    void translate(float dx, float dy) {
        viewport.getCamera().translate(dx, dy, 0);
    }
}
