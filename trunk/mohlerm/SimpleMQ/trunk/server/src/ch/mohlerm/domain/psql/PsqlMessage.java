package ch.mohlerm.domain.psql;

import ch.mohlerm.domain.Message;

/**
 * Created by marcel on 9/23/15.
 */
public class PsqlMessage implements Message {
    private int id;
    private int sender;
    private int receiver;
    private int queue;
    private int timestamp;
    private String message;

    public PsqlMessage(int id, int sender, int receiver, int queue, int timestamp, String message) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.queue = queue;
        this.timestamp = timestamp;
        this.message = message;
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
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(int time1) {
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
}
