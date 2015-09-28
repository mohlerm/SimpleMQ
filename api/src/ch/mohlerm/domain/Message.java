package ch.mohlerm.domain;

/**
 * Created by marcel on 9/21/15.
 */
public interface Message extends DomainObject{
    public int getSender();

    public int getReceiver();

    public int getQueue();
    public void setQueue(int queue);

    public int getTimestamp();
    public void setTimestamp(int time);

    public String getMessage();
    public void setMessage(String message);

}
