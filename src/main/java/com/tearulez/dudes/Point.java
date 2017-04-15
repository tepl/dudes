package com.tearulez.dudes;

import com.badlogic.gdx.math.Vector2;

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

    Vector2 asVector() {
        return new Vector2(x, y);
    }
}
