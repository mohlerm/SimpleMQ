package ch.mohlerm.protocol;

/**
 * Created by marcel on 10/6/15.
 */
public class MessagePassingProtocol {
    public static String init() {
        return "INIT";
    }
    public static boolean validInit(String input) {
        return input.equals(MessagePassingProtocol.init());
    }
    public static String ack(int port) {
        return "ACK::"+String.valueOf(port);
    }
    public static int validAck(String ack) {
        String[] str = ack.split("::");
        if(str[0].equals("ACK")) {
            if(str[1] != null) {
                return Integer.parseInt(str[1]);
            }
        }
        return 0;
    }
    public static String done() {
        return "DONE";
    }
    public static String request(int sender, int receiver, int queue) {
        return "REQ::"+String.valueOf(sender)+"::"+String.valueOf(receiver)+"::"+String.valueOf(queue);
    }
    public static boolean validRequest(String str) {
        return (str.equals("REQ"));
    }
}
