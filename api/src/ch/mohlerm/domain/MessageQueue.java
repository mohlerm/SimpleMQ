package ch.mohlerm.domain;

/**
 * Created by marcel on 9/23/15.
 */
public interface MessageQueue extends DomainObject {
    public void addMessage(Message message);
    public void removeMessage(Message message);
}
