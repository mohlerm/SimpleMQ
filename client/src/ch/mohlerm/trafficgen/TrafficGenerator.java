package ch.mohlerm.trafficgen;

import ch.mohlerm.config.Config;
import ch.mohlerm.protocol.MessagePassingProtocol;
import ch.mohlerm.protocol.SerializableAnswer;
import ch.mohlerm.protocol.SerializableRequest;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 10/7/15.
 */
public abstract class TrafficGenerator implements Runnable {
    // The host:port combination to connect to
    protected SocketChannel socketChannel;
    protected int numberOfRequests = Config.CLIENTAMOUNT;

    public TrafficGenerator(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }


    public abstract void run();


    protected void postRequest(SerializableRequest request) {
        /*
            send client id to initialize on the server and wait for ack (message ID 0)
         */
        ByteBuffer requestBuffer = ByteBuffer.allocate(Config.REQUESTBUFFERSIZE);


        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestBuffer.put(byteOutputStream.toByteArray());

        requestBuffer.flip();

        while (requestBuffer.hasRemaining()) {
            try {
                //log.info("Write message [" + counter + "] to buffer");
                socketChannel.write(requestBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    protected SerializableAnswer getAnswer() throws NoAnswerException {
        ByteBuffer answerBuffer = ByteBuffer.allocate(Config.ANSWERBUFFERSIZE);
        // Attempt to read off the channel
        //log.debug("Attempt to read off channel");
        int numRead;
        try {
            numRead = socketChannel.read(answerBuffer);

        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            try {
                socketChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            throw new NoAnswerException("Forced close");
        }

        if (numRead == -1) {
            //log.debug("Cleanly closed");
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw new NoAnswerException("Cleanly closed");
        }
        if (numRead > 0) {
            return MessagePassingProtocol.parseAnswer(answerBuffer.array(), numRead);

        }
        throw new NoAnswerException("No answer");
    }


}