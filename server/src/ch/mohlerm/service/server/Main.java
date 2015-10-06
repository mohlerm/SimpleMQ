package ch.mohlerm.service.server;

/**
 * Created by marcel on 9/21/15.
 */

import ch.mohlerm.config.Config;
import ch.mohlerm.distributor.Distributor;
import ch.mohlerm.initializer.Initializer;
import ch.mohlerm.queries.SetupQueries;
import org.apache.log4j.Logger;

import java.sql.*;

public class Main {

    static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if(args.length < 2) {
            System.out.println("Please specify <serverid>, <serverport>!");
        } else {
            Config.SERVERID = Integer.parseInt(args[0]);
            log.info("Using server id: " + args[0]);
            Config.SERVERPORT = Integer.parseInt(args[1]);
            log.info("Using server port: " + args[1]);

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
            Distributor distributor = new Distributor();
            Thread distributorThread = new Thread(distributor);
            distributorThread.start();
            Initializer initializer = new Initializer(distributor);
            initializer.run();
        }
    }
}