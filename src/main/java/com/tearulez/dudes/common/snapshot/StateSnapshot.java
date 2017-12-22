package com.tearulez.dudes.common.snapshot;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StateSnapshot implements Serializable {
    private Player player;
    private List<Player> otherPlayers;
    private List<Wall> walls;
    private List<Point> bullets;
    private boolean wasDryFire;
    private boolean wasReload;
    private boolean wasShot;
    private float bulletRadius;
    private float playerRadius;

    public static class Builder {
        private List<Wall> walls = Collections.emptyList();
        private List<Point> bullets;
        private List<Player> otherPlayers;

        public Builder setWalls(List<Wall> walls) {
            this.walls = walls;
            return this;
        }

        public Builder setBullets(List<Point> bullets) {
            this.bullets = bullets;
            return this;
        }

        public Builder setOtherPlayers(List<Player> players) {
            otherPlayers = players;
            return this;
        }

        public StateSnapshot build() {
            return create(
                    Optional.empty(),
                    otherPlayers,
                    walls,
                    bullets,
                    false,
                    false,
                    false,
                    0,
                    0
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
                                       boolean wasShot,
                                       float bulletRadius,
                                       float playerRadius) {
        StateSnapshot state = new StateSnapshot();
        state.player = player.orElse(null);
        state.otherPlayers = otherPlayers;
        state.walls = walls;
        state.bullets = bullets;
        state.wasDryFire = wasDryFire;
        state.wasReload = wasReload;
        state.wasShot = wasShot;
        state.bulletRadius = bulletRadius;
        state.playerRadius = playerRadius;
        return state;
    }

    public static StateSnapshot empty() {
        return create(
                Optional.empty(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                false,
                false,
                false,
                0,
                0
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

    public float getBulletRadius() {
        return bulletRadius;
    }

    public float getPlayerRadius() {
        return playerRadius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateSnapshot that = (StateSnapshot) o;

        if (wasDryFire != that.wasDryFire) return false;
        if (wasReload != that.wasReload) return false;
        if (wasShot != that.wasShot) return false;
        if (bulletRadius != that.bulletRadius) return false;
        if (playerRadius != that.playerRadius) return false;
        if (player != null ? !player.equals(that.player) : that.player != null) return false;
        if (!otherPlayers.equals(that.otherPlayers)) return false;
        if (!walls.equals(that.walls)) return false;
        return bullets.equals(that.bullets);
    }

    @Override
    public int hashCode() {
        int result = player != null ? player.hashCode() : 0;
        result = 31 * result + otherPlayers.hashCode();
        result = 31 * result + walls.hashCode();
        result = 31 * result + bullets.hashCode();
        result = 31 * result + (wasDryFire ? 1 : 0);
        result = 31 * result + (wasReload ? 1 : 0);
        result = 31 * result + (wasShot ? 1 : 0);
        result = 31 * result + Float.valueOf(bulletRadius).hashCode();
        result = 31 * result + Float.valueOf(playerRadius).hashCode();
        return result;
    }
}
