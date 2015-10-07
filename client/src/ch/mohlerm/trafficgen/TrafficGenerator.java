package ch.mohlerm.trafficgen;

import java.io.IOException;
import java.nio.channels.SocketChannel;

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