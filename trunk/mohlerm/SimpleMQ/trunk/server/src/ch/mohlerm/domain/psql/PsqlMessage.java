package ch.mohlerm.domain.psql;

import ch.mohlerm.domain.Message;
import ch.mohlerm.protocol.SerializableRequest;

import java.sql.Timestamp;

/**
 * Created by marcel on 9/23/15.
 */
public class PsqlMessage implements Message {
    private int id;
    private int sender;
    private int receiver;
    private int queue;
    private Timestamp timestamp;
    private String message;

    public PsqlMessage(int id, int sender, int receiver, int queue, Timestamp timestamp, String message) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.queue = queue;
        this.timestamp = timestamp;
        this.message = message;
    }
    public PsqlMessage(SerializableRequest serializableMessage, Timestamp timestamp) {
        this.id = serializableMessage.getId();
        this.sender = serializableMessage.getSource();
        this.receiver = serializableMessage.getTarget();
        this.queue = serializableMessage.getQueue();
        this.timestamp = timestamp;
        this.message = serializableMessage.getMessage();
    }

    @Override
    public int getSender() {
        return sender;
    }

    @Override
    public int getReceiver() {
        return receiver;
    }

    @Override
    public int getQueue() {
        return queue;
    }

    @Override
    public void setQueue(int queue1) {
        queue = queue1;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Timestamp time1) {
        timestamp = time1;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message1) {
        message = message1;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id1) {
        id = id1;
    }

    @Override
    public void setSender(int sender) {
        this.sender = sender;
    }

    @Override
    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }
}
