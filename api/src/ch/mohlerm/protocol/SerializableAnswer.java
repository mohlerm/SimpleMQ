package ch.mohlerm.protocol;

import java.io.Serializable;

/**
 * Created by marcel on 10/13/15.
 */
/*
    This class describes the answer messages used for SimpleMQ

    ENUM | INT    | INT    | INT    | STRING
    TYPE | REQ_ID | CL_ID  | RESULT | MESSAGE

    TYPE:
            ACK:   Correct result -> message in db
            ERROR: No correct result -> message not in db (rejected)
            ANSWERMESSAGE || ANSWERQUEUE: requested id of queue/message
    REQ_ID: Unique message id per client (mainly for debugging purposes)
    CL_ID: ClientId
    RESULT: E.g. ID of message returned
    STRING: Payload of message returned or error message

 */
public class SerializableAnswer implements Serializable {
    public enum AnswerType {
        ACK, ERROR, ANSWERMESSAGE, ANSWERQUEUE;
    }
    AnswerType type;
    int requestId;
    int clientId;
    int resultId;
    String message;

    public SerializableAnswer(AnswerType type, int requestId, int clientId, int resultId, String message) {
        this.type = type;
        this.requestId = requestId;
        this.clientId = clientId;
        this.resultId = resultId;
        if(message == null || message.equals("")) {
            this.message = "null";
        } else {
            this.message = message;
        }
    }

    public AnswerType getType() {
        return type;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getClientId() {
        return clientId;
    }

    public int getResultId() {
        return resultId;
    }

    public String getMessage() {
        return message;
    }
}
