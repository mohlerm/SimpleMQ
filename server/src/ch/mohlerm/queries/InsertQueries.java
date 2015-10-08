package ch.mohlerm.queries;

import ch.mohlerm.domain.psql.PsqlClient;
import ch.mohlerm.domain.psql.PsqlMessage;
import ch.mohlerm.domain.psql.PsqlQueue;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Created by marcel on 9/23/15.
 */
public class InsertQueries {
    /**
     * @param message
     * @param queue
     * @return
     */
    public static int insertMessageIntoQueue(PsqlMessage message, PsqlQueue queue) {
        return 0;
    }

    public static int addMessage(Connection connection, PsqlMessage message) {
        String callableSQL = "{call add_message(?,?,?,?,?)}";
        try {
            CallableStatement callableStatement = connection.prepareCall(callableSQL);
            // sender
            callableStatement.setInt(1,message.getSender());
            // receiver
            callableStatement.setInt(2,message.getReceiver());
            // queue
            callableStatement.setInt(3,message.getQueue());
            // timestamp
            callableStatement.setTimestamp(4,new Timestamp(message.getTimestamp()));
            // message
            callableStatement.setString(5, message.getMessage());
            callableStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // TODO add real return value
        return 0;
    }

    public static int addQueue(Connection connection, PsqlQueue queue) {
        String callableSQL = "{call add_queue()}";
        try {
            CallableStatement callableStatement = connection.prepareCall(callableSQL);
            callableStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // TODO add real return value
        return 0;
    }

    public static int addClient(Connection connection, PsqlClient client) {
        String callableSQL = "{call add_client()}";
        try {
            CallableStatement callableStatement = connection.prepareCall(callableSQL);
            callableStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // TODO add real return value
        return 0;
    }
}
