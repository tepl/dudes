package com.tearulez.dudes;

import java.util.List;

public class Wall {
    private Point position;
    private List<Point> points;

    private Wall() {
    }

    public static Wall create(Point position, List<Point> points) {
        Wall wall = new Wall();
        wall.position = position;
        wall.points = points;
        return wall;
    }

    public List<Point> getPoints() {
        return points;
    }

    public Point getPosition() {
        return position;
    }
}
