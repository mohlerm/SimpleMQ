package ch.mohlerm.queries;

import ch.mohlerm.domain.psql.PsqlMessage;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by marcel on 10/13/15.
 */
public class DeleteQueries {
    public static int deleteClient(Connection connection, int clientId) throws SQLException {
        String callableSQL = "{call delete_client(?)}";
        CallableStatement callableStatement = connection.prepareCall(callableSQL);
        callableStatement.setInt(1, clientId);
        callableStatement.execute();
        // TODO real return
        return 0;
    }
    public static int deleteQueue(Connection connection, int queueId) throws SQLException {
        String callableSQL = "{call delete_queue(?)}";
        CallableStatement callableStatement = connection.prepareCall(callableSQL);
        callableStatement.setInt(1, queueId);
        callableStatement.execute();
        // TODO real return
        return 0;
    }
}
