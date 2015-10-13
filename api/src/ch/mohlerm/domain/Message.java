package ch.mohlerm.domain;

import java.sql.Timestamp;

/**
 * Created by marcel on 9/21/15.
 */
public interface Message extends DomainObject{
    public int getSender();
    public void setSender(int sender);

    public int getReceiver();
    public void setReceiver(int receiver);

    public int getQueue();
    public void setQueue(int queue);

    public Timestamp getTimestamp();
    public void setTimestamp(Timestamp time);

    public String getMessage();
    public void setMessage(String message);

}
