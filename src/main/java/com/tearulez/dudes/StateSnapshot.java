package com.tearulez.dudes;

import com.tearulez.dudes.model.Player;
import com.tearulez.dudes.model.Wall;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StateSnapshot {
    private Player player;
    private List<Player> otherPlayers;
    private List<Wall> walls;
    private List<Point> bullets;
    private boolean wasDryFire;
    private boolean wasReload;
    private boolean wasShot;

    static class Builder {
        private List<Wall> walls = Collections.emptyList();

        Builder setWalls(List<Wall> walls) {
            this.walls = walls;
            return this;
        }

        StateSnapshot build() {
            return create(
                    Optional.empty(),
                    Collections.emptyList(),
                    walls,
                    Collections.emptyList(),
                    false,
                    false,
                    false
            );
        }
    }

    private StateSnapshot() {
    }

    public static StateSnapshot create(Optional<Player> player,
                                       List<Player> otherPlayers,
                                       List<Wall> walls,
                                       List<Point> bullets,
                                       boolean wasDryFire,
                                       boolean wasReload,
                                       boolean wasShot) {
        StateSnapshot state = new StateSnapshot();
        state.player = player.orElse(null);
        state.otherPlayers = otherPlayers;
        state.walls = walls;
        state.bullets = bullets;
        state.wasDryFire = wasDryFire;
        state.wasReload = wasReload;
        state.wasShot = wasShot;
        return state;
    }

    static StateSnapshot empty() {
        return create(
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                false,
                false,
                false
        );
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

    public List<Player> getOtherPlayers() {
        return otherPlayers;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public List<Point> getBullets() {
        return bullets;
    }

    public boolean wasDryFire() {
        return wasDryFire;
    }

    public boolean wasReload() {
        return wasReload;
    }

    public boolean wasShot() {
        return wasShot;
    }
}
