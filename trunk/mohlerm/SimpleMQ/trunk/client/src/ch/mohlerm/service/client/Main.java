package ch.mohlerm.service.client;

import ch.mohlerm.config.client.Config;
import ch.mohlerm.config.client.CustomConfigurationFactory;
import ch.mohlerm.trafficdirect.TrafficDirect;
import ch.mohlerm.trafficgen.TrafficGenerator;
import ch.mohlerm.trafficgen.TrafficGeneratorFactory;
import ch.mohlerm.trafficgen.TrafficGeneratorNotFoundException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.ConfigurationFactory;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Created by marcel on 9/23/15.
 */
public class Main {

   // static Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {

        // basic setup
        if(args.length != 8 ) {
            System.out.println("Please specify <clientid>, <serverip>, <serverport>, <clientTotal>, <traffictype>, <time>, <amount>, <thinktime>!");
        } else {
            String trafficType = args[4];
            if(!(trafficType.equals("staticsmall")||trafficType.equals("staticmedium")||trafficType.equals("staticlarge")||trafficType.equals("directsmall")||trafficType.equals("directlarge"))) {
                System.out.println("Please specify traffictype: staticsmall, staticmedium, staticlarge, directsmall or directlarge");
            } else {
                Config.CLIENTID = Integer.parseInt(args[0]);

                ConfigurationFactory.setConfigurationFactory(new CustomConfigurationFactory());
                Logger log = LogManager.getLogger(Main.class.getName());

                Config.SERVERIP = InetAddress.getByName(args[1]);
                log.info("Using server ip: " + args[1]);
                Config.SERVERPORT = Integer.parseInt(args[2]);
                log.info("Using server port: " + String.valueOf(Config.SERVERPORT));
                Config.CLIENTTOTAL = Integer.parseInt(args[3]);
                log.info("Using total clients: " + String.valueOf(Config.CLIENTTOTAL));
                Config.CLIENTTIME = Integer.parseInt(args[5]);
                log.info("Using time for client requests: " + String.valueOf(Config.CLIENTTIME));
                Config.CLIENTAMOUNT = Integer.parseInt(args[6]);
                log.info("Using amount of client requests: " + String.valueOf(Config.CLIENTAMOUNT));
                Config.CLIENTTHINKTIME = Integer.parseInt(args[7]);
                log.info("Using client thinktime: " + String.valueOf(Config.CLIENTTHINKTIME));
                if(!(trafficType.equals("directsmall")||trafficType.equals("directlarge"))) {
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
                } else {
                    TrafficDirect trafficDirect = new TrafficDirect(trafficType);
                    Thread t = new Thread(trafficDirect);
                    t.start();
                }
            }
        }
    }
}