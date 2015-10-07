package ch.mohlerm.config;

import java.net.InetAddress;

/**
 * Created by marcel on 9/22/15.
 */
public class Config {
    public static String DBURL = "127.0.0.1";
    public static String DBPORT = "5432";
    public static String DBNAME = "testdb";
    public static String DBUSER = "testdb";

    public static int SERVERPORT = 1234;
    public static int SERVERID = 0;
    public static InetAddress SERVERIP;
    public static int CPUWORKERSCALING = 4;
}
