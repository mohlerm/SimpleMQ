package ch.mohlerm.worker;

import ch.mohlerm.config.Config;
import ch.mohlerm.distributor.Distributor;
import ch.mohlerm.domain.psql.PsqlMessage;
import ch.mohlerm.protocol.MessagePassingProtocol;
import ch.mohlerm.protocol.SerializableAnswer;
import ch.mohlerm.protocol.SerializableRequest;
import ch.mohlerm.queries.DeleteQueries;
import ch.mohlerm.queries.InsertQueries;
import ch.mohlerm.queries.SelectQueries;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Created by marcel on 10/6/15.
 */
public class Worker implements Runnable {

    // creates worker, initializes buffer sets up database connection
    public Worker(int id) {
        this.id = id;
        this.request = null;
        this.answer = null;
        log = Logger.getLogger("Server["+String.valueOf(Config.SERVERID)+"]Worker["+String.valueOf(id)+"]");
        setupDBConnection();
    }
    // worker id
    int id;
    //Connection connection;
    SocketChannel socketChannel;
    Distributor callBack;
    SelectionKey key;
    Connection dbConnection;
    Logger log;
    SerializableRequest request;
    SerializableAnswer answer;

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
        log.debug("Got request");
        long startTime = System.nanoTime();
        ByteBuffer requestBuffer = ByteBuffer.allocate(Config.REQUESTBUFFERSIZE);
        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(requestBuffer);
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
            //log.debug(requestBuffer.array());
            request = MessagePassingProtocol.parseRequest(requestBuffer.array(), numRead);
            // split on request type and create appropriate answer
            MessagePassingProtocol.logRequest(request, log);
            int newid;
            PsqlMessage psqlMessage;
            switch (request.getType()) {
                case CREATECLIENT:
                    try {
                        newid = InsertQueries.insertClient(dbConnection, request.getSource());
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.CREATECLIENT, request.getId(), request.getSource(), newid, "OK");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Could not add client with id "+String.valueOf(request.getSource())+"!");
                    }
                    break;
                case DELETECLIENT:
                    try {
                        newid = DeleteQueries.deleteClient(dbConnection, request.getSource());
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.DELETECLIENT, request.getId(), request.getSource(), newid, "OK");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Could not remove client with id "+String.valueOf(request.getSource())+"!");
                    }
                    break;
                case QUERYCLIENT:
                    // TODO QUERYCLIENT
                    break;
                case CREATEQUEUE:
                    try {
                        newid = InsertQueries.insertQueue(dbConnection);
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.CREATEQUEUE, request.getId(), request.getSource(),newid, "OK");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Could not create queue!");
                    }
                    break;
                case DELETEQUEUE:
                    try {
                        newid = DeleteQueries.deleteQueue(dbConnection, request.getQueue());
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.DELETEQUEUE, request.getId(), request.getSource(), newid, "OK");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Could not remove client with id "+String.valueOf(request.getSource())+"!");
                    }
                    break;
                case QUERYQUEUESFORRECEIVER:
                    // TODO QUERYQUEUESFORRRECEIVER
                    break;
                case SENDMESSAGETOALL:
                    psqlMessage = new PsqlMessage(request, new Timestamp(System.currentTimeMillis()));
                    try {
                        // override receiver
                        psqlMessage.setReceiver(0);
                        newid = InsertQueries.insertMessage(dbConnection, psqlMessage);
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.SENDMESSAGETOALL, request.getId(), request.getSource(), newid,"OK");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Can not send message to all.");
                    }
                    break;
                case SENDMESSAGETORECEIVER:
                    psqlMessage = new PsqlMessage(request, new Timestamp(System.currentTimeMillis()));
                    try {
                        newid = InsertQueries.insertMessage(dbConnection, psqlMessage);
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.SENDMESSAGETORECEIVER, request.getId(), request.getSource(), newid,"OK");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Can not send message to receiver: "+request.getTarget());
                    }
                    break;
                case POPQUEUE:
                    try {
                        psqlMessage = SelectQueries.popQueue(dbConnection, request.getQueue(), request.getSource());
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.POPQUEUE, request.getId(), request.getSource(), psqlMessage.getId(), psqlMessage.getMessage());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Could not pop queue "+request.getQueue()+"!");
                    }
                    break;
                case PEEKQUEUE:
                    try {
                        psqlMessage = SelectQueries.peekQueue(dbConnection, request.getQueue(), request.getSource());
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.PEEKQUEUE, request.getId(), request.getSource(), psqlMessage.getId(), psqlMessage.getMessage());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Could not peek queue "+request.getQueue()+"!");
                    }
                    break;
                case QUERYMESSAGESFORSENDER:
                    // TODO QUERYMESSAGESFORSENDER
                    break;
                case QUERYMESSAGESFORRECEIVER:
                    // TODO QUERYMESSAGESFORRECEIVER
                    break;
                default:
                    break;
            }


            ByteBuffer answerBuffer = ByteBuffer.allocate(Config.ANSWERBUFFERSIZE);

            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
                objectOutputStream.writeObject(answer);
                objectOutputStream.flush();
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            answerBuffer.put(byteOutputStream.toByteArray());
            try {
                byteOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            answerBuffer.flip();

            while (answerBuffer.hasRemaining()) {
                try {
                    socketChannel.write(answerBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            long estimatedTime = System.nanoTime() - startTime;
            MessagePassingProtocol.logAnswer(answer, log,estimatedTime);
        }
        log.debug("Finished request");
        callBack.workerCallBack(this);

    }
    private void setupDBConnection() {
        try {
            dbConnection = DriverManager.getConnection(
                    "jdbc:postgresql://" + Config.DBURL + ":" + Config.DBPORT + "/" + Config.DBNAME, Config.DBUSER,
                    null);
        } catch (SQLException e) {
            log.debug("Connection Failed! Check output console");
            e.printStackTrace();
        }
        if (dbConnection != null) {
            log.debug("Connected to Database");
        } else {
            log.debug("Failed to make connection!");
        }

    }

}

