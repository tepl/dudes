package com.tearulez.dudes.common.snapshot;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Point implements Cloneable, Serializable {
    public float x, y = 0;

    private Point() {
    }

    public static Point create(float x, float y) {
        Point point = new Point();
        point.x = x;
        point.y = y;
        return point;
    }

    public Vector2 asVector() {
        return new Vector2(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        return Float.compare(point.x, x) == 0 && Float.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }
}
