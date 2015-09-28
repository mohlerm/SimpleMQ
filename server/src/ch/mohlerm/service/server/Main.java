package ch.mohlerm.service.server;

/**
 * Created by marcel on 9/21/15.
 */

import ch.mohlerm.config.Config;
import ch.mohlerm.protocol.KnockKnockProtocol;
import ch.mohlerm.queries.SetupQueries;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Main {

    static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] argv) {
        log.info("-------- PostgreSQL "
                + "JDBC Connection Testing ------------");
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.info("Where is your PostgreSQL JDBC Driver? "
                    + "Include in your library path!");
            e.printStackTrace();
            return;
        }

        log.info("PostgreSQL JDBC Driver Registered!");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://" + Config.DBURL + ":" + Config.DBPORT + "/" + Config.DBNAME, Config.DBUSER,
                    null);


//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery("SELECT VERSION()");
//
//            if (resultSet.next()) {
//                System.out.println(resultSet.getString(1));
//            }
        } catch (SQLException e) {
            log.info("Connection Failed! Check output console");
            e.printStackTrace();
        }
        try {
            SetupQueries setupQueries = new SetupQueries();
            setupQueries.setupDB(connection);
            log.info("Executed setup queries");
        } catch (SQLException e) {
            log.info("Failed to execute setup queries");
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.info("Can not close connection");
                e.printStackTrace();
            }
        }
        if (connection != null) {
            log.info("You made it, take control your database now!");
        } else {
            log.info("Failed to make connection!");
        }
        // at this point connection is working and established


        try (
                ServerSocket serverSocket = new ServerSocket(Config.SERVICEPORT);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
        ) {

            String inputLine, outputLine;

            // Initiate conversation with client
            KnockKnockProtocol kkp = new KnockKnockProtocol();
            outputLine = kkp.processInput(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = kkp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
        } catch (IOException e) {
            log.error("Exception caught when trying to listen on port "
                    + Config.SERVICEPORT + " or listening for a connection");
            log.info(e.getMessage());
        }

    }
}