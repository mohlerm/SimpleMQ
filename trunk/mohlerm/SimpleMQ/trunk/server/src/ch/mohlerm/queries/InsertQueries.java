package ch.mohlerm.queries;

import ch.mohlerm.domain.psql.PsqlClient;
import ch.mohlerm.domain.psql.PsqlMessage;
import ch.mohlerm.domain.psql.PsqlQueue;

import java.sql.*;

/**
 * Created by marcel on 9/23/15.
 */
public class InsertQueries {
    /**
     * @param message
     * @param queue
     * @return
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

    //    int result = 0;
   //     callableStatement.registerOutParameter(6, Types.INTEGER);
        callableStatement.execute();
//        while(hadResults) {
//            ResultSet rs = callableStatement.getResultSet();
//            rs.
//        }
        // TODO
       // return callableStatement.getInt(6);
        return 0;
    }

    public static int insertQueue(Connection connection) throws SQLException {
        String callableSQL = "{call add_queue()}";
            CallableStatement callableStatement = connection.prepareCall(callableSQL);
            callableStatement.execute();
        // TODO add real return value
        return 0;
    }

    public static int insertClient(Connection connection, int clientId) throws SQLException {
        String callableSQL = "{call add_client(?)}";
        CallableStatement callableStatement = connection.prepareCall(callableSQL);
        // clientId
        callableStatement.setInt(1, clientId);
        callableStatement.execute();
        // todo add real return value
        return clientId;
    }
}
