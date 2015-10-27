package ch.mohlerm.trafficgen;

import ch.mohlerm.config.client.Config;
import ch.mohlerm.config.GlobalConfig;
import ch.mohlerm.protocol.MessagePassingProtocol;
import ch.mohlerm.protocol.SerializableAnswer;
import ch.mohlerm.protocol.SerializableRequest;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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
    protected SerializableAnswer answer;
    protected SerializableRequest request;
    private ByteBuffer messageBuffer;

    public TrafficGenerator(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.messageCounter = 0;
        this.messageBuffer = ByteBuffer.allocate(GlobalConfig.BUFFERSIZE);
        this.log = LogManager.getLogger("Client["+ String.valueOf(Config.CLIENTID)+"]");
    }


    public void run() {
        register();
        if(Config.CLIENTTHINKTIME > 0) {
            // sleep for a predefined time
            try {
                Thread.sleep(Config.CLIENTTHINKTIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        startTraffic();
        if(Config.CLIENTTHINKTIME > 0) {
            // sleep for a predefined time
            try {
                Thread.sleep(Config.CLIENTTHINKTIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        messageCounter++;
        long start = System.nanoTime();
        // +2 because we start with 2 (0 is the register message, 1, the startTraffic , n+2 is the queue delete, n+3 is the endTraffic message)
        while(System.nanoTime() - start < Config.CLIENTTIME*1E9 && (numberOfRequests == -1 || messageCounter < numberOfRequests+2 )) {
        //while((numberOfRequests == -1 || messageCounter < numberOfRequests+2 )) {
        generateTraffic();
            messageCounter++;
            if(Config.CLIENTTHINKTIME > 0) {
                // sleep for a predefined time
                try {
                    Thread.sleep(Config.CLIENTTHINKTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("Done sending " + Config.CLIENTAMOUNT + " messages");
        endTraffic();
        messageCounter++;
        if(Config.CLIENTTHINKTIME > 0) {
            // sleep for a predefined time
            try {
                Thread.sleep(Config.CLIENTTHINKTIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        deregister();
    }

    protected abstract void generateTraffic();
    protected abstract void startTraffic();
    protected abstract void endTraffic();


    protected void postRequest(SerializableRequest request) {
        /*
            send client id to initialize on the server and wait for ack (message ID 0)
         */
        messageBuffer.clear();

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageBuffer.put(byteOutputStream.toByteArray());

        messageBuffer.flip();

        while (messageBuffer.hasRemaining()) {
            try {
                //log.info("Write message [" + counter + "] to buffer");
                socketChannel.write(messageBuffer);
            } catch (IOException e) {
                log.error("Error when writing message to buffer");
                e.printStackTrace();
            }
        }
    }
    protected SerializableAnswer getAnswer() throws NoAnswerException {
        messageBuffer.clear();
        // Attempt to read off the channel
        //log.debug("Attempt to read off channel");
        int numRead;
        try {
            numRead = socketChannel.read(messageBuffer);

        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            try {
                socketChannel.close();
            } catch (IOException e1) {
                log.error("Error when forced closing channel");
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
                log.error("Error when cleanly closing channel");
                e.printStackTrace();
            }
            throw new NoAnswerException("Cleanly closed");
        }
        if (numRead > 0) {
            return MessagePassingProtocol.parseAnswer(messageBuffer.array(), numRead, log);

        }
        throw new NoAnswerException("No answer");
    }

    private void register() {
        request = new SerializableRequest(MessagePassingProtocol.RequestType.CREATECLIENT, messageCounter, Config.CLIENTID, 0, 0, "Empty");
        log.debug("Initialize with client id " + Config.CLIENTID + " on server.");
        long startTime = System.nanoTime();
        postRequest(request);
        MessagePassingProtocol.logRequest(request, log);
        answer = null;
        try {
            log.debug("Wait for client add answer");
            answer = getAnswer();
            long estimatedTime = System.nanoTime() - startTime;
            MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
        } catch (NoAnswerException e) {
            log.error("No client add answer");
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

    private void deregister() {
        request = new SerializableRequest(MessagePassingProtocol.RequestType.DELETECLIENT, messageCounter, Config.CLIENTID, 0, 0, "Empty");
        log.debug("Delete client id " + Config.CLIENTID + " on server.");
        long startTime = System.nanoTime();
        postRequest(request);
        MessagePassingProtocol.logRequest(request, log);
        answer = null;
        try {
            log.debug("Wait for client delete answer");
            answer = getAnswer();
            long estimatedTime = System.nanoTime() - startTime;
            MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
        } catch (NoAnswerException e) {
            log.error("No client delete answer");
            e.printStackTrace();
        }
        log.info("Deregistered");
    }


}