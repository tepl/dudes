package com.tearulez.dudes;

import com.badlogic.gdx.math.Vector2;
import com.tearulez.dudes.common.Messages;
import com.tearulez.dudes.common.snapshot.Player;
import com.tearulez.dudes.common.snapshot.Point;
import com.tearulez.dudes.common.snapshot.StateSnapshot;
import com.tearulez.dudes.common.snapshot.Wall;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class NetworkingSerializationTest {

    @Test
    public void updateModelMessage() throws Exception {
        Messages.UpdateModel msg = new Messages.UpdateModel();
        msg.stateSnapshot = new StateSnapshot.Builder()
                .setWalls(Collections.singletonList(
                        Wall.create(
                                Point.create(0, 0),
                                Arrays.asList(
                                        Point.create(10, 0),
                                        Point.create(0, 10),
                                        Point.create(0, 0)
                                ))))
                .setOtherPlayers(
                        Collections.singletonList(Player.create(
                                Point.create(0, 1),
                                new Vector2(0, 0),
                                100
                        ))
                )
                .setBullets(Arrays.asList(
                        Point.create(0, 0),
                        Point.create(1, 1)
                ))
                .build();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(msg);
        Messages.UpdateModel deserializedMsg =
                (Messages.UpdateModel) new ObjectInputStream(
                        new ByteArrayInputStream(buffer.toByteArray())
                ).readObject();
        assertEquals(msg.stateSnapshot, deserializedMsg.stateSnapshot);
    }
}
