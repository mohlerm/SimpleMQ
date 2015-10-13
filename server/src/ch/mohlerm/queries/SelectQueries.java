package ch.mohlerm.queries;

import ch.mohlerm.domain.psql.PsqlMessage;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by marcel on 9/23/15.
 */
public class SelectQueries {
    public static PsqlMessage popQueue(Connection connection, int queueId, int clientId) throws SQLException {
        String callableSQL = "{call pop_queue(?,?)}";
        CallableStatement callableStatement = connection.prepareCall(callableSQL);
        // queueId
        callableStatement.setInt(1, queueId);
        // clientId
        callableStatement.setInt(2, clientId);
        callableStatement.execute();

        ResultSet rs = callableStatement.getResultSet();
        PsqlMessage result = null;
        while(rs.next()) {
            // sender_id INTEGER, receiver_id INTEGER, queue_id INTEGER, sendtime TIMESTAMP, message TEXT)
            result = new PsqlMessage(rs.getInt(1),rs.getInt(2),rs.getInt(3),rs.getInt(4),rs.getTimestamp(5),rs.getString(6));
        }
        if(result != null) {
            return result;
        } else {
            throw new SQLException("Not found");
        }
    }
    public static PsqlMessage peekQueue(Connection connection, int queueId, int clientId) throws SQLException {
        String callableSQL = "{call peek_queue(?,?)}";
        CallableStatement callableStatement = connection.prepareCall(callableSQL);
        // queueId
        callableStatement.setInt(1, queueId);
        // clientId
        callableStatement.setInt(2, clientId);
        callableStatement.execute();

        ResultSet rs = callableStatement.getResultSet();
        PsqlMessage result = null;
        while(rs.next()) {
            // sender_id INTEGER, receiver_id INTEGER, queue_id INTEGER, sendtime TIMESTAMP, message TEXT)
            result = new PsqlMessage(rs.getInt(1),rs.getInt(2),rs.getInt(3),rs.getInt(4),rs.getTimestamp(5),rs.getString(6));
        }
        if(result != null) {
            return result;
        } else {
            throw new SQLException("Not found");
        }
    }
}
