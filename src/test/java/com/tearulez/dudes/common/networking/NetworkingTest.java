package com.tearulez.dudes.common.networking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class NetworkingTest {
    private static final int PORT = 8080;
    private static final int TIMEOUT = 100;
    private static final String HOST = "localhost";
    private ExecutorService executor = null;
    private Server server = null;

    @Before
    public void before() throws IOException {
        executor = Executors.newSingleThreadExecutor();
        server = new Server();
        server.bind(PORT);
    }

    @After
    public void after() throws IOException {
        executor.shutdownNow();
        server.stop();
    }

    @Test
    public void sendPrimitiveMessages() throws Exception {
        Future<Connection> eventualConnection = executor.submit(server::accept);
        Client client = new Client(HOST, PORT);
        Connection clientConnection = client.connect();
        Connection serverConnection = eventualConnection.get(TIMEOUT, TimeUnit.MILLISECONDS);
        clientConnection.send(0);
        clientConnection.send(1);
        clientConnection.send(2);
        clientConnection.send("test");
        assertEquals(Arrays.asList(0, 1, 2, "test"), serverConnection.receive());
    }

    @Test
    public void sendCustomMessage() throws Exception {
        Future<Connection> eventualConnection = executor.submit(server::accept);
        Client client = new Client(HOST, PORT);
        Connection clientConnection = client.connect();
        Connection serverConnection = eventualConnection.get(TIMEOUT, TimeUnit.MILLISECONDS);
        CustomMessage msg = new CustomMessage();
        msg.x = TIMEOUT;
        msg.y = 100;
        clientConnection.send(msg);
        assertEquals(Collections.singletonList(msg), serverConnection.receive());
    }

    @Test
    public void multipleSendReceive() throws Exception {
        Future<Connection> eventualConnection = executor.submit(server::accept);
        Client client = new Client(HOST, PORT);
        Connection clientConnection = client.connect();
        Connection serverConnection = eventualConnection.get(TIMEOUT, TimeUnit.MILLISECONDS);
        clientConnection.send(0);
        serverConnection.receive();
        clientConnection.send(1);
        assertEquals(Collections.singletonList(1), serverConnection.receive());
    }

    @Test
    public void serverSendsToTheClient() throws Exception {
        Future<Connection> eventualConnection = executor.submit(server::accept);
        Client client = new Client(HOST, PORT);
        Connection clientConnection = client.connect();
        Connection serverConnection = eventualConnection.get(TIMEOUT, TimeUnit.MILLISECONDS);
        serverConnection.send(0);
        assertEquals(Collections.singletonList(0), clientConnection.receive());
    }

    @Test
    public void clientReceivesNothing() throws Exception {
        executor.submit(server::accept);
        Client client = new Client(HOST, PORT);
        Connection clientConnection = client.connect();
        Future<List<Object>> eventualEmptyList = executor.submit(clientConnection::receive);
        eventualEmptyList.get(TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Test(expected = EOFException.class)
    public void clientClosesConnection() throws Exception {
        Future<Connection> eventualConnection = executor.submit(server::accept);
        Client client = new Client(HOST, PORT);
        Connection clientConnection = client.connect();
        Connection serverConnection = eventualConnection.get(TIMEOUT, TimeUnit.MILLISECONDS);
        clientConnection.close();
        serverConnection.receive();
    }

    @Test(expected = EOFException.class)
    public void serverClosesConnection() throws Exception {
        Future<Connection> eventualConnection = executor.submit(server::accept);
        Client client = new Client(HOST, PORT);
        Connection clientConnection = client.connect();
        Connection serverConnection = eventualConnection.get(TIMEOUT, TimeUnit.MILLISECONDS);
        serverConnection.close();
        clientConnection.receive();
    }

    @Test(expected = IOException.class)
    public void writingLotsOfDataToAClosedConnection() throws Exception {
        Future<Connection> eventualConnection = executor.submit(server::accept);
        Client client = new Client(HOST, PORT);
        Connection clientConnection = client.connect();
        Connection serverConnection = eventualConnection.get(TIMEOUT, TimeUnit.MILLISECONDS);
        serverConnection.close();
        for (int i = 0; i < 1000; i++) {
            clientConnection.send("lots of data");
        }
    }

    @Test
    public void partialRead() throws Exception {
        Future<Connection> eventualConnection = executor.submit(server::accept);
        SocketChannel clientSocketChannel = SocketChannel.open();
        clientSocketChannel.connect(new InetSocketAddress(HOST, PORT));
        Connection serverConnection = eventualConnection.get(TIMEOUT, TimeUnit.MILLISECONDS);
        serverConnection.send("big object");

        // send "big object" to the server less one byte
        ByteBuffer buffer = ByteBuffer.allocate(10000);
        clientSocketChannel.read(buffer);
        buffer.flip();
        buffer.limit(buffer.limit() - 1);

        clientSocketChannel.write(buffer);

        assertEquals(Collections.emptyList(), serverConnection.receive());

        // send last byte
        buffer.limit(buffer.limit() + 1);
        clientSocketChannel.write(buffer);

        assertEquals(Collections.singletonList("big object"), serverConnection.receive());
    }

    public static class CustomMessage implements Serializable {
        float x;
        float y;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CustomMessage that = (CustomMessage) o;

            return Float.compare(that.x, x) == 0 && Float.compare(that.y, y) == 0;
        }

        @Override
        public int hashCode() {
            int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
            result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
            return result;
        }
    }

}
