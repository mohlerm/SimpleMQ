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
    private int receiver;
    private int queueNumber;
    private long timer;

    public StaticTrafficGenerator(SocketChannel socketChannel, String fixMessage) throws IOException {
        super(socketChannel);
        this.fixMessage = fixMessage;
        this.receiver = 1;
        //this.log = Logger.getLogger("Client["+ String.valueOf(GlobalConfig.CLIENTID)+"]");
    }

    @Override
    public void generateTraffic() {
        if(receiver==Config.CLIENTID) {
            receiver++;
        }
        if(receiver>Config.CLIENTTOTAL) {
            if(Config.CLIENTID==1) {
                receiver = 2;
            } else {
                receiver = 1;
            }
        }
        // always send a message and then query for one
        if(messageCounter%3 == 1) {
            // (messageCounter%Config.CLIENTTOTAL)+1 sends a message to each client and wraps around
            request = new SerializableRequest(MessagePassingProtocol.RequestType.SENDMESSAGETORECEIVER, messageCounter, Config.CLIENTID, receiver, 1, fixMessage);
            receiver++;
        } else if (messageCounter%3 == 2){
            request = new SerializableRequest(MessagePassingProtocol.RequestType.PEEKQUEUE, messageCounter, Config.CLIENTID, Config.CLIENTID, 1, "");
//            } else if (messageCounter%3 == 3) {
//                request = new SerializableRequest(MessagePassingProtocol.RequestType.SENDMESSAGETOALL, messageCounter, GlobalConfig.CLIENTID, 0, 1, fixMessage);
        } else {
            request = new SerializableRequest(MessagePassingProtocol.RequestType.POPQUEUE, messageCounter, Config.CLIENTID, Config.CLIENTID, 1, "");
        }
        timer = System.nanoTime();
        postRequest(request);
        MessagePassingProtocol.logRequest(request, log);
        answer = null;
        try {
            log.debug("Wait for answer");
            answer = getAnswer();
            long estimatedTime = System.nanoTime() - timer;
            MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
        } catch (NoAnswerException e) {
            log.error("No answer received");
            e.printStackTrace();
        }
    }

    @Override
    protected void startTraffic() {
        request = new SerializableRequest(MessagePassingProtocol.RequestType.CREATEQUEUE, messageCounter, Config.CLIENTID, 0, 1, "");
        timer = System.nanoTime();
        postRequest(request);
        MessagePassingProtocol.logRequest(request,log);
        answer = null;
        queueNumber = 1;
        try {
            log.debug("Wait for create queue answer");
            answer = getAnswer();
            long estimatedTime = System.nanoTime() - timer;
            MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
            queueNumber = answer.getResultId();
        } catch (NoAnswerException e) {
            log.error("No create queue answer received");
            e.printStackTrace();
        }
    }

    @Override
    protected void endTraffic() {
        request = new SerializableRequest(MessagePassingProtocol.RequestType.DELETEQUEUE, messageCounter, Config.CLIENTID, 0, queueNumber, "");
        timer = System.nanoTime();
        postRequest(request);
        MessagePassingProtocol.logRequest(request,log);
        answer = null;
        try {
            log.debug("Wait for delete queue answer");
            answer = getAnswer();
            long estimatedTime = System.nanoTime() - timer;
            MessagePassingProtocol.logAnswer(answer, log, estimatedTime);
            queueNumber = answer.getResultId();
        } catch (NoAnswerException e) {
            log.error("No delete queue answer received");
            e.printStackTrace();
        }
    }

}
