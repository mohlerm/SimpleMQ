package ch.mohlerm.service.server;

/**
 * Created by marcel on 9/21/15.
 */

import ch.mohlerm.config.Config;
import ch.mohlerm.distributor.Distributor;
import ch.mohlerm.queries.psql.PsqlSetupQueries;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        if (args.length < 5) {
            System.out.println("Please specify <serverid>, <serverport>, <workerpercore>, <dbhostname>, <dbport>!");
        } else {
            Config.SERVERID = Integer.parseInt(args[0]);
//            FileAppender fa = new FileAppender();
//            fa.setName("FileLogger");
//            fa.setFile("logs/server_"+String.valueOf(Config.SERVERID)+".log");
//            fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
//            fa.setThreshold(Level.INFO);
//            fa.setAppend(true);
//            fa.activateOptions();
//            Logger.getRootLogger().addAppender(fa);
            log.info("Using server id: " + String.valueOf(Config.SERVERID));
            Config.SERVERPORT = Integer.parseInt(args[1]);
            log.info("Using server port: " + String.valueOf(Config.SERVERPORT));
            Config.SERVERWORKER = Integer.parseInt(args[2]);
            log.info("Using worker: " + String.valueOf(Config.SERVERWORKER));
            Config.DBURL = args[3];
            log.info("Using db ip: " + Config.DBURL);
            Config.DBPORT = args[4];
            log.info("Using db port: " + Config.DBPORT);

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
            if (connection != null) {
                log.info("You made it, take control your database now!");
            } else {
                log.info("Failed to make connection!");
            }
            try {
                // only the first server does this
                if (Config.SERVERID == 1) {
                    PsqlSetupQueries.setupDB(connection);
                    log.info("Executed setup queries");
                }
            } catch (SQLException e) {
                log.info("Failed to execute setup queries");
                e.printStackTrace();
            } finally {
                try {
                    if (connection != null) {
                        log.info("Close connection.");
                        connection.close();
                    }
                } catch (SQLException e) {
                    log.info("Can not close connection");
                    e.printStackTrace();
                }
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
                log.info("Failed to initialize Distributor");
                e.printStackTrace();
            }

            Thread distributorThread = new Thread(distributor);
            distributorThread.start();
        }
    }
}