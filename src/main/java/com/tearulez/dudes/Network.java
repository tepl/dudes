package com.tearulez.dudes;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

import java.util.ArrayList;
import java.util.HashMap;

class Network {
    // This registers objects that are going to be sent over the network.
    static void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(Login.class);
        kryo.register(Registered.class);
        kryo.register(UpdateModel.class);
        kryo.register(MovePlayer.class);
        kryo.register(HashMap.class);
        kryo.register(ArrayList.class);
        kryo.register(Point.class);
        kryo.register(Wall.class);
        kryo.register(GameState.class);
        kryo.register(ShootAt.class);
    }

    static class Login {
    }

    static class Registered {
        int id;
    }

    static class UpdateModel {
        GameState state;
    }

    static class MovePlayer {
        float dx, dy;
    }

    static class ShootAt {
        float x, y;
    }
}
