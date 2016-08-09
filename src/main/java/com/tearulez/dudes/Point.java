package com.tearulez.dudes;

class Point implements Cloneable {
    float x, y = 0;

    private Point() {
    }

    static Point create(float x, float y) {
        Point point = new Point();
        point.x = x;
        point.y = y;
        return point;
    }
}
