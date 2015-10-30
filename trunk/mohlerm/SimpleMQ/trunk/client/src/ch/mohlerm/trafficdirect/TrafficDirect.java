package ch.mohlerm.trafficdirect;

import ch.mohlerm.config.client.Config;
import ch.mohlerm.domain.psql.PsqlMessage;
import ch.mohlerm.protocol.MessagePassingProtocol;
import ch.mohlerm.protocol.SerializableAnswer;
import ch.mohlerm.protocol.SerializableRequest;
import ch.mohlerm.queries.psql.PsqlDeleteQueries;
import ch.mohlerm.queries.psql.PsqlInsertQueries;
import ch.mohlerm.queries.psql.PsqlSelectQueries;
import ch.mohlerm.queries.psql.PsqlSetupQueries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Created by marcel on 10/30/15.
 */
public class TrafficDirect implements Runnable {

    // usually not needed, only in case of trafficdirect
    public static final String DBNAME = "simplemq";
    public static final String DBUSER = "simplemq";

    public TrafficDirect(String type) {
        this.log = LogManager.getLogger("Client["+ String.valueOf(Config.CLIENTID)+"]");
        numberOfRequests = Config.CLIENTAMOUNT;
        if(type.equals("directsmall")) {
            fixMessage = MessagePassingProtocol.smallMessage();
        } else if (type.equals("directlarge")) {
            fixMessage = MessagePassingProtocol.largeMessage();
        } else {
            fixMessage = "ERRORMESSAGE";
        }
        setupDBConnection();

    }

    //Connection connection;
    Connection dbConnection;
    Logger log;
    String fixMessage;

    int numberOfRequests;

    private void setupDBConnection() {
        try {
            dbConnection = DriverManager.getConnection(
                    "jdbc:postgresql://" + Config.SERVERIP.getHostAddress() + ":" + Config.SERVERPORT + "/" + DBNAME, DBUSER,
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

    @Override
    public void run() {
        int messageCounter = 0;
        long start = System.nanoTime();
        int receiver = 1;
        // first insert own id
        SerializableAnswer answer;
        SerializableRequest request;
        PsqlMessage psqlMessage;

        // only the first client does this
        if(Config.CLIENTID==1) {
            try {
                PsqlSetupQueries.setupDB(dbConnection);
            } catch (SQLException e) {
                log.error("Could not issue setup queries");
                e.printStackTrace();
            }
        }

        request = new SerializableRequest(MessagePassingProtocol.RequestType.CREATECLIENT, messageCounter, Config.CLIENTID, 0, 0, "Empty");
        log.debug("Initialize with client id " + Config.CLIENTID + " on server.");
        MessagePassingProtocol.logRequest(request, log);
        long queryStartTime = System.nanoTime();
        try {
            int newid = PsqlInsertQueries.insertClient(dbConnection, request.getSource());
            answer = new SerializableAnswer(MessagePassingProtocol.RequestType.CREATECLIENT, request.getId(), request.getSource(), newid, "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Could not add client with id " + String.valueOf(request.getSource()) + "!");
        }
        long queryTime = System.nanoTime() - queryStartTime;
        MessagePassingProtocol.logAnswerWithQuery(answer, log, 0, queryTime);

        try {
            Thread.sleep(Config.INITWAIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Registered");
        messageCounter++;


        // now run for some time
        while(System.nanoTime() - start < Config.CLIENTTIME*1E9 && (numberOfRequests == -1 || messageCounter < numberOfRequests+2 )) {
            if (receiver == Config.CLIENTID) {
                receiver++;
            }
            if (receiver > Config.CLIENTTOTAL) {
                if (Config.CLIENTID == 1) {
                    receiver = 2;
                } else {
                    receiver = 1;
                }
            }
            // always send a message and then query for one
            if (messageCounter % 3 == 1) {
                // (messageCounter%Config.CLIENTTOTAL)+1 sends a message to each client and wraps around
                request = new SerializableRequest(MessagePassingProtocol.RequestType.SENDMESSAGETORECEIVER, messageCounter, Config.CLIENTID, receiver, 1, fixMessage);
                receiver++;
            } else if (messageCounter % 3 == 2) {
                request = new SerializableRequest(MessagePassingProtocol.RequestType.PEEKQUEUE, messageCounter, Config.CLIENTID, Config.CLIENTID, 1, "");
            } else {
                request = new SerializableRequest(MessagePassingProtocol.RequestType.POPQUEUE, messageCounter, Config.CLIENTID, Config.CLIENTID, 1, "");
            }
            MessagePassingProtocol.logRequest(request, log);
            int newid;
           // int result;
            queryStartTime = System.nanoTime();
            switch (request.getType()) {
                case SENDMESSAGETORECEIVER:
                    psqlMessage = new PsqlMessage(request, new Timestamp(System.currentTimeMillis()));
                    try {
                        newid = PsqlInsertQueries.insertMessage(dbConnection, psqlMessage);
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.SENDMESSAGETORECEIVER, request.getId(), request.getSource(), newid, "OK");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Can not send message to receiver: " + request.getTarget());
                    }
                    break;
                case POPQUEUE:
                    try {
                        psqlMessage = PsqlSelectQueries.popQueue(dbConnection, request.getQueue(), request.getSource());
                        if (psqlMessage.getId() > 0) {
                            answer = new SerializableAnswer(MessagePassingProtocol.RequestType.POPQUEUE, request.getId(), request.getSource(), psqlMessage.getId(), psqlMessage.getMessage());
                        } else {
                            answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "No message to pop available on queue " + request.getQueue() + "!");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Could not pop queue " + request.getQueue() + "!");
                    }
                    break;
                case PEEKQUEUE:
                    try {
                        psqlMessage = PsqlSelectQueries.peekQueue(dbConnection, request.getQueue(), request.getSource());
                        if (psqlMessage.getId() > 0) {
                            answer = new SerializableAnswer(MessagePassingProtocol.RequestType.PEEKQUEUE, request.getId(), request.getSource(), psqlMessage.getId(), psqlMessage.getMessage());
                        } else {
                            answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "No message to peek available on queue " + request.getQueue() + "!");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Could not peek queue " + request.getQueue() + "!");
                    }
                    break;
                default:
                    break;
            }
            queryTime = System.nanoTime() - queryStartTime;
            MessagePassingProtocol.logAnswerWithQuery(answer, log, 0, queryTime);
            messageCounter++;
            if(Config.CLIENTTHINKTIME > 0) {
                // sleep for a predefined time
                try {
                    Thread.sleep(Config.CLIENTTHINKTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        request = new SerializableRequest(MessagePassingProtocol.RequestType.DELETECLIENT, messageCounter, Config.CLIENTID, 0, 0, "Empty");
        log.debug("Delete client id " + Config.CLIENTID + " on server.");
        MessagePassingProtocol.logRequest(request, log);
        queryStartTime = System.nanoTime();
        try {
            int newid = PsqlDeleteQueries.deleteClient(dbConnection, request.getSource());
            answer = new SerializableAnswer(MessagePassingProtocol.RequestType.DELETECLIENT, request.getId(), request.getSource(), newid, "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            answer = new SerializableAnswer(MessagePassingProtocol.RequestType.ERROR, request.getId(), request.getSource(), -1, "Could not remove client with id " + String.valueOf(request.getSource()) + "!");
        }
        queryTime = System.nanoTime() - queryStartTime;
        MessagePassingProtocol.logAnswerWithQuery(answer, log, 0, queryTime);
        log.info("Deregistered");
    }
}
