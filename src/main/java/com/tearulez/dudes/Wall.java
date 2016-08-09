package com.tearulez.dudes;

import java.util.List;

class Wall {
    private List<Point> points;

    private Wall() {
    }

    static Wall create(List<Point> points) {
        Wall wall = new Wall();
        wall.points = points;
        return wall;
    }

    List<Point> getPoints() {
        return points;
    }
}
