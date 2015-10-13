package ch.mohlerm.trafficgen;

import ch.mohlerm.protocol.MessagePassingProtocol;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 10/7/15.
 */
public class TrafficGeneratorFactory {
    public static TrafficGenerator getTrafficGenerator(SocketChannel socketChannel, String trafficType) throws IOException, TrafficGeneratorNotFoundException {
        if(trafficType.substring(0,6).equals("static")) {
            if (trafficType.equals("staticsmall")) {
                return new StaticTrafficGenerator(socketChannel, MessagePassingProtocol.smallMessage());
            } else if (trafficType.equals("staticmedium")) {
                return new StaticTrafficGenerator(socketChannel, MessagePassingProtocol.mediumMessage());
            } else if (trafficType.equals("staticlarge")) {
                return new StaticTrafficGenerator(socketChannel, MessagePassingProtocol.largeMessage());
            } else {
                throw new TrafficGeneratorNotFoundException("No such traffic generator");
            }
        } else {
            throw new TrafficGeneratorNotFoundException("No such traffic generator");
        }
    }
}
