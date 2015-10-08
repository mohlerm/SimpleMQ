package ch.mohlerm.trafficgen;

import ch.mohlerm.config.Config;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 10/7/15.
 */
public abstract class TrafficGenerator implements Runnable {
    // The host:port combination to connect to
    protected SocketChannel socketChannel;
    protected int numberOfRequests = Config.CLIENTAMOUNT;

    public TrafficGenerator(SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
    }


    public abstract void run();


}