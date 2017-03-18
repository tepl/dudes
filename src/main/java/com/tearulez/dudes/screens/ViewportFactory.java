package com.tearulez.dudes.screens;

import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ViewportFactory {
    private final float ratio;

    public ViewportFactory(float ratio) {
        this.ratio = ratio;
    }

    Viewport createViewport(float height) {
        return new ScalingViewport(Scaling.fillY, height * ratio, height);
    }
}
