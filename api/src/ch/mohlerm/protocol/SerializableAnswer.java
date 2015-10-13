package ch.mohlerm.protocol;

import ch.mohlerm.domain.DomainObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by marcel on 10/13/15.
 */
public class SerializableAnswer implements Serializable {
    public enum AnswerType {
        ACK, ERROR, ANSWERMESSAGE, ANSWERQUEUE;
    }
    AnswerType type;
    int requestId;
    int resultId;
    String message;

    public SerializableAnswer(AnswerType type, int requestId, int resultId, String message) {
        this.type = type;
        this.requestId = requestId;
        this.resultId = resultId;
        this.message = message;
    }

    public AnswerType getType() {
        return type;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getResultId() {
        return resultId;
    }

    public String getMessage() {
        return message;
    }
}
