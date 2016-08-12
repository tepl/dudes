package com.tearulez.dudes;

class Player {
    static final int MAX_HEALTH = 100;
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

    Point getPosition() {
        return position;
    }

    int getHealth() {
        return health;
    }
}
