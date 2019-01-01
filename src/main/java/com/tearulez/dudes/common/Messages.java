package com.tearulez.dudes.common;

import com.tearulez.dudes.common.snapshot.Point;
import com.tearulez.dudes.common.snapshot.StateSnapshot;

import java.io.Serializable;

public class Messages {

    // From server to client

    public static class SpawnResponse implements Serializable {
        public boolean success;
    }

    public static class UpdateModel implements Serializable {
        public StateSnapshot stateSnapshot;
    }

    public static class PlayerDeath implements Serializable {
    }

    // From client to server

    public static class MovePlayer implements Serializable {
        public float dx, dy;
    }

    public static class RotatePlayer implements Serializable {
        public float angle;
    }

    public static class ShootAt implements Serializable {
        public float x, y;
    }

    public static class SpawnRequest implements Serializable {
        public Point startingPosition;
    }

    public static class Reload implements Serializable {
    }
}
