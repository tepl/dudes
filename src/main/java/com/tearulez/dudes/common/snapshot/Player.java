package com.tearulez.dudes.common.snapshot;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Player implements Serializable {
    public static final int MAX_HEALTH = 100;
    private Point position;
    private Vector2 velocity;
    private float angle;
    private int health;

    private Player() {
    }

    public static Player create(Point position, Vector2 velocity, float angle, int health) {
        Player player = new Player();
        player.position = position;
        player.velocity = velocity;
        player.angle = angle;
        player.health = health;
        return player;
    }

    public Point getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity.cpy();
    }

    public float getAngle() {
        return angle;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return health == player.health && position.equals(player.position);
    }

    @Override
    public int hashCode() {
        int result = position.hashCode();
        result = 31 * result + health;
        return result;
    }
}
