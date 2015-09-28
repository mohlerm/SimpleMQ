package ch.mohlerm.service.client;

import ch.mohlerm.config.Config;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.*;
/**
 * Created by marcel on 9/23/15.
 */
public class Main {

    static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {


        try (
                Socket kkSocket = new Socket(Config.SERVICEHOST, Config.SERVICEPORT);
                PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(kkSocket.getInputStream()));
        ) {
            BufferedReader stdIn =
                    new BufferedReader(new InputStreamReader(System.in));
            String fromServer;
            String fromUser;

            while ((fromServer = in.readLine()) != null) {
                log.info("Server: " + fromServer);
                if (fromServer.equals("Bye."))
                    break;

                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    log.info("Client: " + fromUser);
                    out.println(fromUser);
                }
            }
        } catch (UnknownHostException e) {
            log.error("Don't know about host " + Config.SERVICEHOST);
            System.exit(1);
        } catch (IOException e) {
            log.error("Couldn't get I/O for the connection to " +
                    Config.SERVICEHOST);
            System.exit(1);
        }
    }
}