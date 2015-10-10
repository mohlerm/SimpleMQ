package ch.mohlerm.worker;

import ch.mohlerm.config.Config;
import ch.mohlerm.distributor.Distributor;
import ch.mohlerm.domain.psql.PsqlMessage;
import ch.mohlerm.queries.InsertQueries;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by marcel on 10/6/15.
 */
public class Worker implements Runnable {

    // creates worker and sets up database connection
    public Worker() {
        setupDBConnection();
    }
    //Connection connection;
    SocketChannel socketChannel;
    Distributor callBack;
    SelectionKey key;
    Connection dbConnection;
    static Logger log = Logger.getLogger(Worker.class.getName());

    //    public void setConnection(Connection connection) {
//        this.connection = connection;
//    }
    public void setChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void setCallBack(Distributor callBack) {
        this.callBack = callBack;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

    @Override
    public void run() {
        log.debug("Worker got request");
        // Clear out our read buffer so it's ready for new data
        ByteBuffer readBuffer = ByteBuffer.allocate(8192);

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(readBuffer);
        } catch (IOException e) {
            log.debug("Forced close");
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            try {
                socketChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            callBack.workerCallBack(this);
            return;
        }

        if (numRead == -1) {
            log.debug("Cleanly closed");
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            try {
                key.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            key.cancel();
            callBack.workerCallBack(this);
            return;
        }
        if (numRead > 0) {
            String messageContent = processData(readBuffer.array(), numRead);
            PsqlMessage message = new PsqlMessage(-1,1,2,1,121212,messageContent);
            InsertQueries.addMessage(dbConnection, message);
            String newData = "SERVER " + String.valueOf(Config.SERVERID) + " Answer";

            ByteBuffer buf = ByteBuffer.allocate(48);
            buf.clear();
            buf.put(newData.getBytes());

            buf.flip();

            while (buf.hasRemaining()) {
                try {
                    socketChannel.write(buf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        callBack.workerCallBack(this);

    }
    private String processData(byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        String result = new String(dataCopy);
        log.info(result);
        return result;
    }

    private void setupDBConnection() {
        try {
            dbConnection = DriverManager.getConnection(
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
        if (dbConnection != null) {
            log.debug("Worker connected to Database");
        } else {
            log.debug("Failed to make connection!");
        }

//        try {
//            SetupQueries setupQueries = new SetupQueries();
//            setupQueries.setupDB(connection);
//            log.debug("Executed setup queries");
//        } catch (SQLException e) {
//            log.debug("Failed to execute setup queries");
//            e.printStackTrace();
//        } finally {
//            try {
//                if (connection != null) {
//                    connection.close();
//                }
//            } catch (SQLException e) {
//                log.debug("Can not close connection");
//                e.printStackTrace();
//            }
//        }


    }
}

