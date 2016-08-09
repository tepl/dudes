package com.tearulez.dudes;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

import java.util.ArrayList;
import java.util.HashMap;

public class Network {
    static public final int port = 54555;

    // This registers objects that are going to be sent over the network.
    static public void register(EndPoint endPoint) {
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
    }

    static public class Login {
    }

    static public class Registered {
        public int id;
    }

    static public class UpdateModel {
        public GameState state;
    }

    static public class MovePlayer {
        public float dx, dy;
    }
}
