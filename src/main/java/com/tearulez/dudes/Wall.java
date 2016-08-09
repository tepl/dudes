package com.tearulez.dudes;

import java.util.List;

public class Wall {
    private List<Point> points;

    public Wall() {
    }

    public static Wall create(List<Point> points) {
        Wall wall = new Wall();
        wall.points = points;
        return wall;
    }

    public List<Point> getPoints() {
        return points;
    }
}
