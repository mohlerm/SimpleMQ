package ch.mohlerm.trafficgen;

import ch.mohlerm.config.Config;
import ch.mohlerm.protocol.MessagePassingProtocol;
import ch.mohlerm.protocol.SerializableAnswer;
import ch.mohlerm.protocol.SerializableRequest;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 10/7/15.
 */
public class StaticTrafficGenerator extends TrafficGenerator {
    private final Logger log;
    private final String fixMessage;

    public StaticTrafficGenerator(SocketChannel socketChannel, String fixMessage) throws IOException {
        super(socketChannel);
        this.fixMessage = fixMessage;
        this.log = Logger.getLogger("Client["+ String.valueOf(Config.CLIENTID)+"]");
    }

    @Override

    public void run() {
        int counter = 0;

        SerializableRequest request = new SerializableRequest(MessagePassingProtocol.RequestType.CREATECLIENT, counter, Config.CLIENTID, 0, 0, "Empty");
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

        counter++;
        /*
            this is the main loop
          */
        while(counter < numberOfRequests+1) {

            // always send a message and then query for one
            if(counter%3 == 1) {
                request = new SerializableRequest(MessagePassingProtocol.RequestType.SENDMESSAGETOALL, counter, Config.CLIENTID, 0, 1, fixMessage);
            } else if (counter%3 == 2){
                request = new SerializableRequest(MessagePassingProtocol.RequestType.PEEKQUEUE, counter, Config.CLIENTID, 0, 1, "");
            } else {
                //request = new SerializableRequest(SerializableRequest.RequestType.SENDMESSAGETOALL, counter, Config.CLIENTID, 0, 1, fixMessage);
                request = new SerializableRequest(MessagePassingProtocol.RequestType.POPQUEUE, counter, Config.CLIENTID, 0, 1, "");
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

            // sleep for a predefined time
            try {
                Thread.sleep(Config.CLIENTPAUSE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counter++;
        }

        request = new SerializableRequest(MessagePassingProtocol.RequestType.DELETECLIENT, counter, Config.CLIENTID, 0, 0, "Empty");
        log.debug("Delete client id " + Config.CLIENTID + " on server.");
        startTime = System.nanoTime();
        postRequest(request);
        MessagePassingProtocol.logRequest(request, log);
        answer = null;
        try {
            log.debug("Wait for init answer");
            answer = getAnswer();
            long estimatedTime = System.nanoTime() - startTime;
            MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
        } catch (NoAnswerException e) {
            e.printStackTrace();
        }

    }

}
