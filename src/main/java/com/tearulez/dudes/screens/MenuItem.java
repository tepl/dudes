package com.tearulez.dudes.screens;

public class MenuItem {

    private final String name;
    private final Runnable callback;

    public MenuItem(String name, Runnable callback) {
        this.name = name;
        this.callback = callback;
    }

    String getName() {
        return name;
    }

    Runnable getCallback() {
        return callback;
    }
}
