package ch.mohlerm.queries;

import ch.mohlerm.domain.psql.PsqlClient;
import ch.mohlerm.domain.psql.PsqlMessage;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by marcel on 9/23/15.
 */
public class SelectQueries {
    public static PsqlMessage popQueue(Connection connection, int queueId) throws SQLException {
        String callableSQL = "{call pop_queue(?)}";
        CallableStatement callableStatement = connection.prepareCall(callableSQL);
        // clientId
        callableStatement.setInt(1, queueId);
        callableStatement.execute();
        // TODO implement
        return null;
        //return new PsqlMessage()
    }
}
