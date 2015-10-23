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
        // +1 because we start with 1 (0 is the register message, n+1 is the deregister message)
        long startTime;
        SerializableRequest request;
        SerializableAnswer answer;
        while(messageCounter < numberOfRequests+1) {

            // always send a message and then query for one
            if(messageCounter%3 == 1) {
                request = new SerializableRequest(MessagePassingProtocol.RequestType.SENDMESSAGETOALL, messageCounter, Config.CLIENTID, 0, 1, fixMessage);
            } else if (messageCounter%3 == 2){
                request = new SerializableRequest(MessagePassingProtocol.RequestType.PEEKQUEUE, messageCounter, Config.CLIENTID, 0, 1, "");
//            } else if (messageCounter%3 == 3) {
//                request = new SerializableRequest(MessagePassingProtocol.RequestType.SENDMESSAGETOALL, messageCounter, GlobalConfig.CLIENTID, 0, 1, fixMessage);
            } else {
                request = new SerializableRequest(MessagePassingProtocol.RequestType.POPQUEUE, messageCounter, Config.CLIENTID, 0, 1, "");
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
        log.info("Done sending " + Config.CLIENTAMOUNT + " messages");
    }

}
