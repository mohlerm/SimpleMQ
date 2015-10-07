package ch.mohlerm.service.server;

/**
 * Created by marcel on 9/21/15.
 */

import ch.mohlerm.config.Config;
import ch.mohlerm.distributor.Distributor;
import ch.mohlerm.queries.SetupQueries;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            Config.SERVERIP = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (args.length < 2) {
            System.out.println("Please specify <serverid>, <serverport>!");
        } else {
            Config.SERVERID = Integer.parseInt(args[0]);
            log.debug("Using server id: " + args[0]);
            Config.SERVERPORT = Integer.parseInt(args[1]);
            log.debug("Using server port: " + args[1]);

            log.debug("-------- PostgreSQL "
                    + "JDBC Connection Testing ------------");
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                log.debug("Where is your PostgreSQL JDBC Driver? "
                        + "Include in your library path!");
                e.printStackTrace();
                return;
            }

            log.debug("PostgreSQL JDBC Driver Registered!");
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
                log.debug("Connection Failed! Check output console");
                e.printStackTrace();
            }
            try {
                SetupQueries setupQueries = new SetupQueries();
                setupQueries.setupDB(connection);
                log.debug("Executed setup queries");
            } catch (SQLException e) {
                log.debug("Failed to execute setup queries");
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    log.debug("Can not close connection");
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                log.debug("You made it, take control your database now!");
            } else {
                log.debug("Failed to make connection!");
            }
            // at this point connection is working and established
//            Distributor distributor = null;
//            try {
//                distributor = new Distributor();
//            } catch (IOException e) {
//                log.error("Failed to initialize Distributor");
//                e.printStackTrace();
//            }
         //   Thread distributorThread = new Thread(distributor);
          //  distributorThread.start();
            Distributor distributor = null;
            try {
                distributor = new Distributor();
            } catch (IOException e) {
                log.error("Failed to initialize Initializer");
                e.printStackTrace();
            }

            Thread distributorThread = new Thread(distributor);
            distributorThread.start();
        }
    }
}