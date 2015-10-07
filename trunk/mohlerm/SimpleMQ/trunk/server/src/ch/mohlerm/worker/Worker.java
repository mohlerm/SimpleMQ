package ch.mohlerm.worker;

import ch.mohlerm.distributor.Distributor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 10/6/15.
 */
public class Worker implements Runnable {

    //Connection connection;
    SocketChannel socketChannel;
    Distributor callBack;
    SelectionKey key;
    static Logger log = Logger.getLogger(Worker.class.getName());

    //    public void setConnection(Connection connection) {
//        this.connection = connection;
//    }
    public void setChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void setCallBack(Distributor callBack) {
        this.callBack = callBack;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

    @Override
    public void run() {
        log.debug("Worker got request");
        // Clear out our read buffer so it's ready for new data
        ByteBuffer readBuffer = ByteBuffer.allocate(8192);

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(readBuffer);
        } catch (IOException e) {
            log.debug("Forced close");
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
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
                key.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            key.cancel();
            return;
        }
        if (numRead > 0) {
            logData(readBuffer.array(), numRead);
        }
        callBack.workerCallBack(this);

    }
    public void logData(byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        log.info(new String(dataCopy));
    }
}

