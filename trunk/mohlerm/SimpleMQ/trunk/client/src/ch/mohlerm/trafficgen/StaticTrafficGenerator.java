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
public class StaticTrafficGenerator extends TrafficGenerator {
    static Logger log = Logger.getLogger(StaticTrafficGenerator.class.getName());
    private final String fixMessage;

    public StaticTrafficGenerator(SocketChannel socketChannel, String fixMessage) throws IOException {
        super(socketChannel);
        this.fixMessage = fixMessage;
    }

    @Override

    public void run() {
        int counter = 0;

        SerializableRequest request = new SerializableRequest(SerializableRequest.RequestType.CREATECLIENT, counter, Config.CLIENTID, 0, 0, "Empty");
        log.debug("Initialize with client id " + Config.CLIENTID + " on server.");
        postRequest(request);
        MessagePassingProtocol.logRequest(request, log);
        SerializableAnswer answer = null;
        try {
            log.debug("Wait for init answer");
            answer = getAnswer();
            MessagePassingProtocol.logAnswer(answer, log);
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
//            if(counter%2 == 0) {
                request = new SerializableRequest(SerializableRequest.RequestType.SENDMESSAGETOALL, counter, Config.CLIENTID, 0, 1, fixMessage);
//            } else {
//                request = new SerializableRequest(SerializableRequest.RequestType.POPQUEUE, counter, Config.CLIENTID, 0, 1, "");
//            }
            postRequest(request);
            MessagePassingProtocol.logRequest(request, log);
            answer = null;
            try {
                log.debug("Wait for answer");
                answer = getAnswer();
                MessagePassingProtocol.logAnswer(answer, log);
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

        request = new SerializableRequest(SerializableRequest.RequestType.DELETECLIENT, counter, Config.CLIENTID, 0, 0, "Empty");
        log.debug("Delete client id " + Config.CLIENTID + " on server.");
        postRequest(request);
        MessagePassingProtocol.logRequest(request, log);
        answer = null;
        try {
            log.debug("Wait for init answer");
            answer = getAnswer();
            MessagePassingProtocol.logAnswer(answer, log);
        } catch (NoAnswerException e) {
            e.printStackTrace();
        }

    }

}
