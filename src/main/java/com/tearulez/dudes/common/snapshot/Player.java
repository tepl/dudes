package com.tearulez.dudes.common.snapshot;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Player implements Serializable {
    public static final int MAX_HEALTH = 100;
    private Point position;
    private Vector2 velocity;
    private int health;

    private Player() {
    }

    public static Player create(Point position, Vector2 velocity, int health) {
        Player player = new Player();
        player.position = position;
        player.velocity = velocity;
        player.health = health;
        return player;
    }

    public Point getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity.cpy();
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
