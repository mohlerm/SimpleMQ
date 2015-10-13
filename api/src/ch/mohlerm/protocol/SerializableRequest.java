package ch.mohlerm.protocol;

import java.io.Serializable;

/**
 * Created by marcel on 10/13/15.
 */
/*
    This class describes the messages used for SimpleMQ

    ENUM | INT    | INT    | INT    | INT   | TIME | STRING
    TYPE | ID     | SOURCE | TARGET | QUEUE | TIME | MESSAGE

    TYPE:
            Queue: CreateQueue, DeleteQueue, QueryQueuesForReceiver
            Message: send: SendMessageToAll, SendMessageToReceiver, PopQueue, PeekQueue
                      receive: QueryMessagesForSender, QueryMessagesForReceiver
    ID: Unique message id per client (mainly for debugging purposes)
    SOURCE: ClientId
    TARGET: ReceiverId

 */
public class SerializableRequest implements Serializable {
    public enum RequestType {
        CREATECLIENT, QUERYCLIENT, DELETECLIENT,
        CREATEQUEUE, DELETEQUEUE, QUERYQUEUESFORRECEIVER,
        SENDMESSAGETOALL, SENDMESSAGETORECEIVER, POPQUEUE, PEEKQUEUE,
        QUERYMESSAGESFORSENDER, QUERYMESSAGESFORRECEIVER
    }
    RequestType type;
    int id;
    int source;
    int target;
    int queue;
    String message;

    public SerializableRequest(RequestType type, int id, int source, int target, int queue, String message) {
        this.type = type;
        this.id = id;
        this.source = source;
        this.target = target;
        this.queue = queue;
        if(message == null || message.equals("")) {
            this.message = "null";
        } else {
            this.message = message;
        }
    }

    public RequestType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public int getQueue() {
        return queue;
    }

    public String getMessage() {
        return message;
    }
}
