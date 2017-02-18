package com.tearulez.dudes.model;

import com.tearulez.dudes.Point;

public class Player {
    public static final int MAX_HEALTH = 100;
    private Point position;
    private int health;

    private Player() {
    }

    static Player create(Point position, int health) {
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
}
