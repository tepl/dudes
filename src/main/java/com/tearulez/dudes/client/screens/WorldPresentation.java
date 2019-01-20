package com.tearulez.dudes.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
    private static final float GRASS_TILE_SIZE = 25;
    private static final float ROOF_TILE_SIZE = 20;
    private static final int NUMBER_OF_CIRCLE_SEGMENTS = 8;
    private final GameState state;
    private Viewport viewport;

    // Sounds
    private final SoundSettings soundSettings;
    private Sound dryFireSound = Gdx.audio.newSound(Gdx.files.internal("res/sounds/dryfire.mp3"));
    private Sound reloadingSound = Gdx.audio.newSound(Gdx.files.internal("res/sounds/reload.mp3"));
    private Sound shotSound = Gdx.audio.newSound(Gdx.files.internal("res/sounds/M4A1.mp3"));

    // Graphics
    private TextureRegion grassTex = new TextureRegion(new Texture(Gdx.files.internal("res/images/grass.png")));
    private TextureRegion wallTex = new TextureRegion(new Texture(Gdx.files.internal("res/images/wall.png")));
    private Sprite playerSprite = new Sprite(new Texture(Gdx.files.internal("res/images/dude.png")));
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private SpriteBatch spriteBatch = new SpriteBatch();
    private PolygonSpriteBatch polyBatch = new PolygonSpriteBatch();
    private EarClippingTriangulator triangulator;

    public WorldPresentation(ViewportFactory viewportFactory, GameState state, SoundSettings soundSettings) {
        viewport = viewportFactory.createViewport(VIEWPORT_HEIGHT);
        this.state = state;
        this.soundSettings = soundSettings;

        // Graphics
        triangulator = new EarClippingTriangulator();
        grassTex.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        wallTex.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
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
        Gdx.gl.glClearColor(0.1f, 0.3f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Vector2 p = viewport.unproject(new Vector2(viewport.getScreenX(), viewport.getScreenY()));
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        float u = p.x / GRASS_TILE_SIZE;
        float v = -p.y / GRASS_TILE_SIZE;
        float u2 = u + w / GRASS_TILE_SIZE;
        float v2 = v + h / GRASS_TILE_SIZE;
        grassTex.setRegion(u, v, u2, v2);
        spriteBatch.begin();
        spriteBatch.draw(grassTex, p.x, p.y - h, w, h);
        spriteBatch.end();
    }

    private void renderBullets(List<Point> bullets, float bulletRadius) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Point bullet : bullets) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.circle(bullet.x, bullet.y, bulletRadius, NUMBER_OF_CIRCLE_SEGMENTS);
        }
        shapeRenderer.end();
    }

    private void renderPlayers(Optional<Player> playerOpt, List<Player> otherPlayers, float playerRadius) {

        // Render player sprites
        spriteBatch.begin();
        for (Player otherPlayer : otherPlayers) {
            renderPlayerSprite(otherPlayer, Color.RED, playerRadius);
        }
        playerOpt.ifPresent(player -> renderPlayerSprite(player, Color.GREEN, playerRadius));
        spriteBatch.end();

        // Render player healths
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Player otherPlayer : otherPlayers) {
            renderPlayerHealth(otherPlayer, playerRadius);
        }
        playerOpt.ifPresent(player -> renderPlayerHealth(player, playerRadius));
        shapeRenderer.end();
    }

    private void renderPlayerSprite(Player player, Color color, float playerRadius) {
        Point position = player.getPosition();
        float angle = player.getAngle() * MathUtils.radiansToDegrees;
        playerSprite.setSize(2 * playerRadius, 2 * playerRadius);
        playerSprite.setOrigin(playerRadius, playerRadius);
        playerSprite.setRotation(angle);
        playerSprite.setPosition(position.x - playerRadius, position.y - playerRadius);
        playerSprite.setColor(color);
        playerSprite.draw(spriteBatch);
    }

    private void renderPlayerHealth(Player player, float r) {
        Point p = player.getPosition();
        float f = (float) player.getHealth() / Player.MAX_HEALTH;
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(p.x - r, p.y + r, 2 * r, r / 4);
        shapeRenderer.setColor(Color.OLIVE);
        shapeRenderer.rect(p.x - r, p.y + r, 2 * r * f, r / 4);
    }

    private void renderWalls(List<Wall> walls) {
        polyBatch.begin();
        for (Wall wall : walls) {
            int size = wall.getPoints().size();
            float[] vertices = new float[size * 2];
            for (int i = 0; i < size; i++) {
                Point point = wall.getPoints().get(i);
                vertices[i * 2] = point.x;
                vertices[i * 2 + 1] = point.y;
            }
            PolygonRegion polyReg = new PolygonRegion(wallTex, vertices, triangulator.computeTriangles(vertices).toArray());
            computeTextureCoords(polyReg);
            Point position = wall.getPosition();
            polyBatch.draw(polyReg, position.x, position.y);
        }
        polyBatch.end();
    }

    private void computeTextureCoords(PolygonRegion polyReg) {
        float[] coords = polyReg.getTextureCoords();
        float[] vertices = polyReg.getVertices();
        TextureRegion texReg = polyReg.getRegion();

        Vector2 p0 = new Vector2(vertices[0], vertices[1]);
        Vector2 p1 = new Vector2(vertices[2], vertices[3]);
        Vector2 p2 = new Vector2(vertices[4], vertices[5]);
        p2.sub(p1);
        p1.sub(p0);
        Vector2 p = p2.len2() < p1.len2() ? p2 : p1;
        p.nor();
        float cos = p.x;
        float sin = p.y;

        for (int i = 0; i < vertices.length; i += 2) {
            float u = texReg.getU() + vertices[i] / ROOF_TILE_SIZE;
            float v = texReg.getV() + vertices[i + 1] / ROOF_TILE_SIZE;
            coords[i] = cos * u + sin * v;
            coords[i + 1] = -sin * u + cos * v;
        }
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
