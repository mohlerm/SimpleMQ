package ch.mohlerm.trafficgen;

import ch.mohlerm.config.Config;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 10/7/15.
 */
public class StaticTrafficGenerator extends TrafficGenerator {
    static Logger log = Logger.getLogger(StaticTrafficGenerator.class.getName());

    public StaticTrafficGenerator(SocketChannel socketChannel) throws IOException {
        super(socketChannel);
    }

    @Override

    public void run() {
        long counter = 0;
        while(counter < numberOfRequests) {
            String newData = "CLIENT " + String.valueOf(Config.CLIENTID) + " Number: " + counter++;

            ByteBuffer buf = ByteBuffer.allocate(48);
            buf.clear();
            buf.put(newData.getBytes());

            buf.flip();

            while (buf.hasRemaining()) {
                try {
                    log.info("Write message [" + counter + "] to buffer");
                    socketChannel.write(buf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            buf.flip();
            // Attempt to read off the channel
            int numRead;
            try {
                numRead = socketChannel.read(buf);
                log.info("Read message [" + counter + "] from buffer");
            } catch (IOException e) {
                log.debug("Forced close");
                // The remote forcibly closed the connection, cancel
                // the selection key and close the channel.
                try {
                    socketChannel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }

            if (numRead == -1) {
                log.debug("Cleanly closed");
                // Remote entity shut the socket down cleanly. Do the
                // same from our end and cancel the channel.
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (numRead > 0) {
                logData(buf.array(), numRead);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    public void logData(byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        log.info(new String(dataCopy));
    }
}
