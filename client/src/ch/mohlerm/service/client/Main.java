package ch.mohlerm.service.client;

import ch.mohlerm.config.Config;
import ch.mohlerm.trafficgen.TrafficGenerator;
import ch.mohlerm.trafficgen.TrafficGeneratorFactory;
import org.apache.log4j.Logger;

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

        // basic setu
        if(args.length < 2) {
            System.out.println("Please specify <clientid>, <serverip>, (<serverport>)!");
        } else {
            Config.CLIENTID = Integer.parseInt(args[0]);
            Config.SERVERIP = InetAddress.getByName(args[1]);
            log.info("Using server ip: " + args[1]);
            if(args.length == 3) {
                Config.SERVERPORT = Integer.parseInt(args[2]);
                log.info("Overriding server port: " + args[2]);
            }
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(Config.SERVERIP, Config.SERVERPORT));
            TrafficGenerator trafficGenerator;
            trafficGenerator = TrafficGeneratorFactory.getTrafficGenerator(socketChannel);
            Thread t = new Thread(trafficGenerator);
            t.start();
        }
    }
}