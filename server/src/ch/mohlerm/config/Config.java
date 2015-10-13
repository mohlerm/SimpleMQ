package ch.mohlerm.config;

/**
 * Created by marcel on 9/22/15.
 */
public class Config {
    public static String DBURL = "127.0.0.1";
    public static String DBPORT = "51230";
    public static String DBNAME = "simplemq";
    public static String DBUSER = "simplemq";

    public static int SERVERPORT = 1234;
    public static int SERVERID = 0;
   // public static InetAddress SERVERIP;
    public static int CPUWORKERSCALING = 4;
    public static int REQUESTBUFFERSIZE = 4096;
    public static int ANSWERBUFFERSIZE = 1024;
}
