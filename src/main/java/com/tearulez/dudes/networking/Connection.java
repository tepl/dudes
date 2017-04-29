package com.tearulez.dudes.networking;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Connection {
    private static final int INT_SIZE = 4;
    private final SocketChannel channel;
    private ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

    public Connection(SocketChannel channel) {
        this.channel = channel;
    }

    public void send(Object obj) throws IOException {
        byte[] byteArray = serialize(obj);
        ByteBuffer buf = ByteBuffer.allocate(byteArray.length + INT_SIZE);
        buf.putInt(byteArray.length);
        buf.put(byteArray);
        buf.flip();
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
    }

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(obj);
        out.flush();
        return buffer.toByteArray();
    }

    private Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
    }

    public List<Object> receive() throws IOException, ClassNotFoundException {
        int read = channel.read(buffer);
        if (read == -1) {
            throw new EOFException();
        }
        buffer.flip();
        List<Object> objs = new ArrayList<>();
        while (true) {
            if (buffer.remaining() < INT_SIZE) {
                break;
            }
            int size = buffer.getInt();
            if (buffer.remaining() < size) {
                buffer.position(buffer.position() - INT_SIZE);
                break;
            }
            byte[] bytes = new byte[size];
            buffer.get(bytes);
            objs.add(deserialize(bytes));
        }
        buffer.compact();
        return objs;
    }

    public void close() throws IOException {
        channel.close();
    }

}
