package ch.mohlerm.initializer;

import ch.mohlerm.config.Config;
import ch.mohlerm.distributor.Distributor;
import ch.mohlerm.protocol.KnockKnockProtocol;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by marcel on 10/5/15.
 */
public class Initializer implements Runnable {
    static Logger log = Logger.getLogger(Initializer.class.getName());
    // number of connected clients
    private int nrOfClients = 0;
    // number of invalid socket ports
    private int skipped = 0;
    Distributor distributor;

    public Initializer(Distributor distributor) {
        this.distributor = distributor;
    }
    @Override
    public void run() {
        try (
            ServerSocket serverSocket = new ServerSocket(Config.SERVERPORT);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine, outputLine;

            // Initiate conversation with client
//            KnockKnockProtocol kkp = new KnockKnockProtocol();
//            outputLine = kkp.processInput(null);
//            out.println(outputLine);

 //           while ((inputLine = in.readLine()) != null) {
                //outputLine = kkp.processInput(inputLine);
                //out.println(outputLine);
//                if (outputLine.equals("Bye."))
//                    break;
           // }
            while ((inputLine = in.readLine()) != null) {
                if(validRequest(inputLine)) {
                    // if we have a valid request we make a new Connection object with
                    // a new unique port
                    int uniquePort = Config.SERVERPORT+1+Config.SERVERID+nrOfClients;
                    ServerSocket serverSocket1 = new ServerSocket();
                    Socket clientSocket1 = serverSocket1.accept();
                    Connection connection = new Connection(clientSocket1, uniquePort);
                    distributor.addConnection(connection);
                    nrOfClients++;
                }
            }
        } catch (IOException e) {
            log.error("Exception caught when trying to listen on port "
                    + Config.SERVERPORT + " or listening for a connection");
            log.info(e.getMessage());
        }
    }
    private boolean validRequest(String input) {
        return input.startsWith("request");
    }
}
