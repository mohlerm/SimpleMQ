package ch.mohlerm.trafficgen;

import ch.mohlerm.config.Config;
import ch.mohlerm.config.GlobalConfig;
import ch.mohlerm.protocol.MessagePassingProtocol;
import ch.mohlerm.protocol.SerializableAnswer;
import ch.mohlerm.protocol.SerializableRequest;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 10/7/15.
 */
public abstract class TrafficGenerator implements Runnable {
    // The host:port combination to connect to
    protected SocketChannel socketChannel;
    protected int numberOfRequests = Config.CLIENTAMOUNT;
    protected Logger log;
    protected int messageCounter;

    public TrafficGenerator(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.messageCounter = 0;
        this.log = Logger.getLogger("Client["+ String.valueOf(Config.CLIENTID)+"]");
    }


    public void run() {
        register();
        generateTraffic();
        deregister();
    }

    protected abstract void generateTraffic();


    protected void postRequest(SerializableRequest request) {
        /*
            send client id to initialize on the server and wait for ack (message ID 0)
         */
        ByteBuffer requestBuffer = ByteBuffer.allocate(GlobalConfig.REQUESTBUFFERSIZE);


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
        ByteBuffer answerBuffer = ByteBuffer.allocate(GlobalConfig.ANSWERBUFFERSIZE);
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

    protected void register() {
        SerializableRequest request = new SerializableRequest(MessagePassingProtocol.RequestType.CREATECLIENT, messageCounter, Config.CLIENTID, 0, 0, "Empty");
        log.debug("Initialize with client id " + Config.CLIENTID + " on server.");
        long startTime = System.nanoTime();
        postRequest(request);
        MessagePassingProtocol.logRequest(request, log);
        SerializableAnswer answer = null;
        try {
            log.debug("Wait for init answer");
            answer = getAnswer();
            long estimatedTime = System.nanoTime() - startTime;
            MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
        } catch (NoAnswerException e) {
            e.printStackTrace();
        }
        // sleep for a predefined time
        try {
            Thread.sleep(Config.INITWAIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Registered");
        messageCounter++;
    }

    protected void deregister() {
        SerializableRequest request = new SerializableRequest(MessagePassingProtocol.RequestType.DELETECLIENT, messageCounter, Config.CLIENTID, 0, 0, "Empty");
        log.debug("Delete client id " + Config.CLIENTID + " on server.");
        long startTime = System.nanoTime();
        postRequest(request);
        MessagePassingProtocol.logRequest(request, log);
        SerializableAnswer answer = null;
        try {
            log.debug("Wait for init answer");
            answer = getAnswer();
            long estimatedTime = System.nanoTime() - startTime;
            MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
        } catch (NoAnswerException e) {
            e.printStackTrace();
        }
        log.info("Deregistered");
    }


}