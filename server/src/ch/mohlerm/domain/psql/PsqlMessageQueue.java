package ch.mohlerm.domain.psql;

import ch.mohlerm.domain.Message;
import ch.mohlerm.domain.MessageQueue;

import java.util.Stack;

/**
 * Created by marcel on 9/23/15.
 */
public class PsqlMessageQueue implements MessageQueue {
    private Stack<Message> messageStack;
    private int id;

    public PsqlMessageQueue() {
        messageStack = new Stack<Message>();
    }

    @Override
    public void addMessage(Message message) {
        messageStack.add(message);
    }

    @Override
    public void removeMessage(Message message) {

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
