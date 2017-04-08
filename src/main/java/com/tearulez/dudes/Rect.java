package com.tearulez.dudes;

public class Rect {
    private float xmin;
    private float xmax;
    private float ymin;
    private float ymax;

    public Rect(float xmin, float xmax, float ymin, float ymax) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
    }

    Point getRandomPoint() {
        float x = xmin + (xmax - xmin) * (float) Math.random();
        float y = ymin + (ymax - ymin) * (float) Math.random();
        return Point.create(x, y);
    }
}
