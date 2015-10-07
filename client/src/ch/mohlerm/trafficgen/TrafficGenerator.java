package ch.mohlerm.trafficgen;

import ch.mohlerm.config.Config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

/**
 * Created by marcel on 10/7/15.
 */
public abstract class TrafficGenerator implements Runnable {
    // The host:port combination to connect to
    protected SocketChannel socketChannel;

    public TrafficGenerator(SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
    }


    public abstract void run();


}