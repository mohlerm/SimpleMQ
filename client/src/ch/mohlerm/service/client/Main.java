package ch.mohlerm.service.client;

import ch.mohlerm.config.Config;
import ch.mohlerm.trafficgen.TrafficGenerator;
import ch.mohlerm.trafficgen.TrafficGeneratorFactory;
import ch.mohlerm.trafficgen.TrafficGeneratorNotFoundException;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 9/23/15.
 */
public class Main {

    static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {

        // basic setup
        if(args.length != 5 ) {
            System.out.println("Please specify <clientid>, <serverip>, <serverport>, <traffictype>, <amount>!");
        } else {
            String trafficType = args[3];
            if(!(trafficType.equals("staticsmall")||trafficType.equals("staticmedium")||trafficType.equals("staticlarge"))) {
                System.out.println("Please specify traffictype: staticsmall, staticmedium or staticlarge");
            } else {
                Config.CLIENTID = Integer.parseInt(args[0]);

                FileAppender fa = new FileAppender();
                fa.setName("FileLogger");
                fa.setFile("logs/client_" + Config.CLIENTID + ".log");
                fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
                fa.setThreshold(Level.INFO);
                fa.setAppend(true);
                fa.activateOptions();
                Logger.getRootLogger().addAppender(fa);

                Config.SERVERIP = InetAddress.getByName(args[1]);
                log.info("Using server ip: " + args[1]);
                Config.SERVERPORT = Integer.parseInt(args[2]);
                log.info("Using server port: " + args[2]);
                Config.CLIENTAMOUNT = Integer.parseInt(args[4]);
                log.info("Using amount of client requests: " + args[4]);
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress(Config.SERVERIP, Config.SERVERPORT));
                TrafficGenerator trafficGenerator = null;
                try {
                    trafficGenerator = TrafficGeneratorFactory.getTrafficGenerator(socketChannel, trafficType);
                    Thread t = new Thread(trafficGenerator);
                    t.start();
                } catch (TrafficGeneratorNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}