package ch.mohlerm.queries;

import ch.mohlerm.domain.psql.PsqlMessage;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by marcel on 9/23/15.
 */
public class InsertQueries {
    /**
     * @param connection the database connection
     * @param message the message to insert
     * @return int the id of the inserted message
     */
    public static int insertMessage(Connection connection, PsqlMessage message) throws SQLException{
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

        ResultSet rs = callableStatement.getResultSet();
        int result = -1;
        while(rs.next()) {
            result = rs.getInt(1);
        }
        return result;
    }

    public static int insertQueue(Connection connection) throws SQLException {
        String callableSQL = "{call add_queue()}";
        CallableStatement callableStatement = connection.prepareCall(callableSQL);
        callableStatement.execute();

        ResultSet rs = callableStatement.getResultSet();
        int result = -1;
        while(rs.next()) {
            result = rs.getInt(1);
        }
        return result;
    }

    public static int insertClient(Connection connection, int clientId) throws SQLException {
        String callableSQL = "{call add_client(?)}";
        CallableStatement callableStatement = connection.prepareCall(callableSQL);
        // clientId
        callableStatement.setInt(1, clientId);
        callableStatement.execute();
        ResultSet rs = callableStatement.getResultSet();
        int result = -1;
        while(rs.next()) {
            result = rs.getInt(1);
        }
        return result;
    }
}
