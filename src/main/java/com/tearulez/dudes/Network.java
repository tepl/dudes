package com.tearulez.dudes;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.tearulez.dudes.model.Player;
import com.tearulez.dudes.model.Wall;

import java.util.ArrayList;
import java.util.HashMap;

public class Network {
    public static final int MAX_OBJECT_SIZE = 102400;
    public static final int WRITE_BUFFER_SIZE = MAX_OBJECT_SIZE * 5;

    // This registers objects that are going to be sent over the network.
    public static void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(Login.class);
        kryo.register(Respawned.class);
        kryo.register(UpdateModel.class);
        kryo.register(MovePlayer.class);
        kryo.register(HashMap.class);
        kryo.register(ArrayList.class);
        kryo.register(Point.class);
        kryo.register(Wall.class);
        kryo.register(StateSnapshot.class);
        kryo.register(ShootAt.class);
        kryo.register(Player.class);
        kryo.register(PlayerDeath.class);
        kryo.register(RespawnRequest.class);
        kryo.register(Reload.class);
    }

    public static class Login {
    }

    public static class Respawned {
    }

    public static class UpdateModel {
        public StateSnapshot stateSnapshot;
    }

    public static class MovePlayer {
        public float dx, dy;
    }

    public static class ShootAt {
        public float x, y;
    }

    public static class PlayerDeath {
    }

    public static class RespawnRequest {
        public Point startingPosition;
    }

    public static class Reload {
    }
}
