package com.tearulez.dudes.client.screens;

import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ViewportFactory {
    private static final float ratio = 10;

    Viewport createViewport(float height) {
        return new ExtendViewport(height / ratio, height, height * ratio, height);
    }
}
