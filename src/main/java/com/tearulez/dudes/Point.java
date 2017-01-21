package com.tearulez.dudes;

public class Point implements Cloneable {
    public float x, y = 0;

    private Point() {
    }

    public static Point create(float x, float y) {
        Point point = new Point();
        point.x = x;
        point.y = y;
        return point;
    }
}
