package ch.mohlerm.trafficgen;

import ch.mohlerm.config.client.Config;
import ch.mohlerm.protocol.MessagePassingProtocol;
import ch.mohlerm.protocol.SerializableAnswer;
import ch.mohlerm.protocol.SerializableRequest;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 10/7/15.
 */
public class StaticTrafficGenerator extends TrafficGenerator {
    //private final Logger log;
    private final String fixMessage;

    public StaticTrafficGenerator(SocketChannel socketChannel, String fixMessage) throws IOException {
        super(socketChannel);
        this.fixMessage = fixMessage;
        //this.log = Logger.getLogger("Client["+ String.valueOf(GlobalConfig.CLIENTID)+"]");
    }

    @Override
    public void generateTraffic() {
        /*
            this is the main loop
          */

        long startTime;
        SerializableRequest request;
        SerializableAnswer answer;
        request = new SerializableRequest(MessagePassingProtocol.RequestType.CREATEQUEUE, messageCounter, Config.CLIENTID, 0, 1, "");

        startTime = System.nanoTime();
        postRequest(request);
        MessagePassingProtocol.logRequest(request,log);
        answer = null;
        int queueNumber = 1;
        try {
            log.debug("Wait for answer");
            answer = getAnswer();
            long estimatedTime = System.nanoTime() - startTime;
            MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
            queueNumber = answer.getResultId();
        } catch (NoAnswerException e) {
            e.printStackTrace();
        }
        if(Config.CLIENTPAUSE > 0) {
            // sleep for a predefined time
            try {
                Thread.sleep(Config.CLIENTPAUSE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        messageCounter++;
        // +2 because we start with 2 (0 is the register message, 1, the queue create, n+2 is the queue delete, n+3 is the deregister message)
        while(messageCounter < numberOfRequests+2) {

            // always send a message and then query for one
            if(messageCounter%3 == 1) {
                // (messageCounter%Config.CLIENTTOTAL)+1 sends a message to each client and wraps around
                request = new SerializableRequest(MessagePassingProtocol.RequestType.SENDMESSAGETORECEIVER, messageCounter, Config.CLIENTID, (messageCounter%Config.CLIENTTOTAL)+1, 1, fixMessage);
            } else if (messageCounter%3 == 2){
                request = new SerializableRequest(MessagePassingProtocol.RequestType.PEEKQUEUE, messageCounter, Config.CLIENTID, Config.CLIENTID, 1, "");
//            } else if (messageCounter%3 == 3) {
//                request = new SerializableRequest(MessagePassingProtocol.RequestType.SENDMESSAGETOALL, messageCounter, GlobalConfig.CLIENTID, 0, 1, fixMessage);
            } else {
                request = new SerializableRequest(MessagePassingProtocol.RequestType.POPQUEUE, messageCounter, Config.CLIENTID, Config.CLIENTID, 1, "");
            }
            startTime = System.nanoTime();
            postRequest(request);
            MessagePassingProtocol.logRequest(request, log);
            answer = null;
            try {
                log.debug("Wait for answer");
                answer = getAnswer();
                long estimatedTime = System.nanoTime() - startTime;
                MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
            } catch (NoAnswerException e) {
                e.printStackTrace();
            }

            if(Config.CLIENTPAUSE > 0) {
                // sleep for a predefined time
                try {
                    Thread.sleep(Config.CLIENTPAUSE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            messageCounter++;
        }
        request = new SerializableRequest(MessagePassingProtocol.RequestType.DELETEQUEUE, messageCounter, Config.CLIENTID, 0, queueNumber, "");
        startTime = System.nanoTime();
        postRequest(request);
        MessagePassingProtocol.logRequest(request,log);
        answer = null;
        try {
            log.debug("Wait for answer");
            answer = getAnswer();
            long estimatedTime = System.nanoTime() - startTime;
            MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
            queueNumber = answer.getResultId();
        } catch (NoAnswerException e) {
            e.printStackTrace();
        }
        if(Config.CLIENTPAUSE > 0) {
            // sleep for a predefined time
            try {
                Thread.sleep(Config.CLIENTPAUSE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Done sending " + Config.CLIENTAMOUNT + " messages");
    }

}
