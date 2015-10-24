package ch.mohlerm.queries.psql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by marcel on 9/27/15.
 */
public class PsqlSetupQueries {
    /**
     * sets up the database
     *
     * @param connection
     * @return true if successful
     * @throws SQLException
     */
    public static void setupDB(Connection connection) throws SQLException {
//        String queryString = "DROP TABLE IF EXISTS messages, queues, message_queues;";
//        Statement statement = connection.createStatement();
//        queryString = "CREATE TABLE IF NOT EXISTS messages (id integer NOT NULL, sender_id integer, receiver_id integer, queue_id integer, timestamp date, message VARCHAR(25), Constraint message_pkey Primary Key (id));";
//        statement = connection.createStatement();
//        statement.execute(queryString);
//        queryString = "CREATE TABLE IF NOT EXISTS queues   (id integer NOT NULL, Constraint queue_pkey Primary Key (id));";
//        statement = connection.createStatement();
//        statement.execute(queryString);
//        queryString = "CREATE TABLE IF NOT EXISTS message_queues (message_id integer NOT NULL, queue_id integer NOT NULL, Constraint message_queue_pkey Primary Key (message_id, queue_id));";
//        statement = connection.createStatement();
//        statement.execute(queryString);
        // create the 0 client (catchall)
          PsqlInsertQueries.insertClient(connection, 0);
        // create initial queue
          //PsqlInsertQueries.insertQueue(connection);
    }
}
