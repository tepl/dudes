package com.tearulez.dudes.common.networking;

import com.tearulez.dudes.server.Assertions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {

    private ServerSocketChannel serverSocketChannel;

    public void bind(int port) throws IOException {
        Assertions.require(serverSocketChannel == null, "server should not be running");
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
    }

    public Connection accept() throws IOException {
        SocketChannel channel = serverSocketChannel.accept();
        Assertions.require(channel != null, "channel shouldn't be null");
        channel.configureBlocking(false);
        return new Connection(channel);
    }

    public void stop() throws IOException {
        serverSocketChannel.socket().close();
    }
}
