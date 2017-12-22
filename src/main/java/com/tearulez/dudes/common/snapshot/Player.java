package com.tearulez.dudes.common.snapshot;

import java.io.Serializable;

public class Player implements Serializable {
    public static final int MAX_HEALTH = 100;
    private Point position;
    private int health;

    private Player() {
    }

    public static Player create(Point position, int health) {
        Player player = new Player();
        player.position = position;
        player.health = health;
        return player;
    }

    public Point getPosition() {
        return position;
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
