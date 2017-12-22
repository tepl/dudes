package com.tearulez.dudes.common.snapshot;

import java.io.Serializable;
import java.util.List;

public class Wall implements Serializable {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wall wall = (Wall) o;

        if (!position.equals(wall.position)) return false;
        return points.equals(wall.points);
    }

    @Override
    public int hashCode() {
        int result = position.hashCode();
        result = 31 * result + points.hashCode();
        return result;
    }
}
