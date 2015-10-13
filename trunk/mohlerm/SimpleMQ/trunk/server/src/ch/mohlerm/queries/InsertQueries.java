package ch.mohlerm.queries;

import ch.mohlerm.domain.psql.PsqlClient;
import ch.mohlerm.domain.psql.PsqlMessage;
import ch.mohlerm.domain.psql.PsqlQueue;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

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

    public static int addMessage(Connection connection, PsqlMessage message) throws SQLException{
        String callableSQL = "{call add_message(?,?,?,?,?)}";
        CallableStatement callableStatement = connection.prepareCall(callableSQL);
        // sender
        callableStatement.setInt(1,message.getSender());
        // receiver
        callableStatement.setInt(2,message.getReceiver());
        // queue
        callableStatement.setInt(3,message.getQueue());
        // timestamp
        callableStatement.setTimestamp(4,message.getTimestamp());
        // message
        callableStatement.setString(5, message.getMessage());
        callableStatement.execute();

        // TODO add real return value
        return 0;
    }

    public static int addQueue(Connection connection, PsqlQueue queue) throws SQLException {
        String callableSQL = "{call add_queue()}";
            CallableStatement callableStatement = connection.prepareCall(callableSQL);
            callableStatement.execute();
        // TODO add real return value
        return 0;
    }

    public static int addClient(Connection connection, PsqlClient client) throws SQLException {
        String callableSQL = "{call add_client(?)}";
        CallableStatement callableStatement = connection.prepareCall(callableSQL);
        // clientId
        callableStatement.setInt(1, client.getId());
        callableStatement.execute();
        return client.getId();
    }
}
