package com.tearulez.dudes.model;

class PlayerId {
    private int id;

    private PlayerId() {
    }

    static PlayerId create(int id) {
        PlayerId playerId = new PlayerId();
        playerId.id = id;
        return playerId;
    }

    int getPlayerId() {
        return id;
    }
}
