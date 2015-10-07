package ch.mohlerm.trafficgen;

import ch.mohlerm.config.Config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by marcel on 10/7/15.
 */
public class StaticTrafficGenerator extends TrafficGenerator {

    public StaticTrafficGenerator(SocketChannel socketChannel) throws IOException {
        super(socketChannel);
    }

    @Override

    public void run() {
        long counter = 0;
        while(true) {
            String newData = "CLIENT " + String.valueOf(Config.CLIENTID) + " Number: " + counter++;

            ByteBuffer buf = ByteBuffer.allocate(48);
            buf.clear();
            buf.put(newData.getBytes());

            buf.flip();

            while (buf.hasRemaining()) {
                try {
                    socketChannel.write(buf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
