package ch.mohlerm.worker;

import ch.mohlerm.distributor.Distributor;
import ch.mohlerm.initializer.Connection;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by marcel on 10/6/15.
 */
public class Worker implements Runnable {

    Connection connection;
    Distributor callBack;
    static Logger log = Logger.getLogger(Worker.class.getName());

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    public void setCallBack(Distributor callBack) {
        this.callBack = callBack;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getSocket().getInputStream()));)
        {
            String inputString = in.readLine();
            log.info(inputString);
            System.out.println(inputString);
            PrintWriter out = new PrintWriter(connection.getSocket().getOutputStream(), true);
            out.println("ACK");
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
