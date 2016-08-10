package com.tearulez.dudes;

import java.util.List;

class Wall {
    private Point position;
    private List<Point> points;

    private Wall() {
    }

    static Wall create(Point position, List<Point> points) {
        Wall wall = new Wall();
        wall.position = position;
        wall.points = points;
        return wall;
    }

    List<Point> getPoints() {
        return points;
    }

    Point getPosition() {
        return position;
    }
}
