package ch.mohlerm.initializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by marcel on 10/5/15.
 */
public class Connection {
    volatile boolean busy;
    int port;
    Socket socket;

    public Connection(Socket socket, int port) {
        this.port = port;
        this.socket = socket;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public boolean messageWaiting() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));)
        {
            char[] buff = new char[3];
            in.read(buff,0,3);
            in.close();
            return (buff.equals("req"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }
}
