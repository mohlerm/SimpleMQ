package ch.mohlerm.config;

import java.net.InetAddress;

/**
 * Created by marcel on 9/23/15.
 */
public class Config {
    // DEFAULT VALUES, can be overridden by specifying the command line parameters
    public static int SERVERPORT = 1234;
    public static InetAddress SERVERIP;
    public static int CLIENTID = 0;
    public static int SERVERREQUESTPORT = 0;
}
