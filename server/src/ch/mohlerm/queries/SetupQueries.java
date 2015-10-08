package ch.mohlerm.queries;

import ch.mohlerm.domain.psql.PsqlClient;
import ch.mohlerm.domain.psql.PsqlQueue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by marcel on 9/27/15.
 */
public class SetupQueries {
    /**
     * sets up the database
     *
     * @param connection
     * @return true if successful
     * @throws SQLException
     */
    public void setupDB(Connection connection) throws SQLException {
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
          InsertQueries.addClient(connection, new PsqlClient());
          InsertQueries.addClient(connection, new PsqlClient());
          InsertQueries.addQueue(connection, new PsqlQueue());
    }
}
