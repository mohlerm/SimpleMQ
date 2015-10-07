package ch.mohlerm.trafficgen;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 10/7/15.
 */
public class TrafficGeneratorFactory {
    public static TrafficGenerator getTrafficGenerator(SocketChannel socketChannel) throws IOException {
        return new StaticTrafficGenerator(socketChannel);
    }
}
