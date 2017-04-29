package com.tearulez.dudes;

import java.io.Serializable;

public class Network {
    public static class SpawnResponse implements Serializable {
        public boolean success;
    }

    public static class UpdateModel implements Serializable {
        public StateSnapshot stateSnapshot;
    }

    public static class MovePlayer implements Serializable {
        public float dx, dy;
    }

    public static class ShootAt implements Serializable {
        public float x, y;
    }

    public static class PlayerDeath implements Serializable {
    }

    public static class SpawnRequest implements Serializable {
        public Point startingPosition;
    }

    public static class Reload implements Serializable {
    }
}
