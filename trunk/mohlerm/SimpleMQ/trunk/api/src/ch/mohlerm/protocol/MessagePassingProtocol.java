package ch.mohlerm.protocol;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by marcel on 10/6/15.
 */

public class MessagePassingProtocol {
    public static String smallMessage() {
        return lorem.substring(0,200);
    }
    public static String mediumMessage() {
        return lorem.substring(0,1100);
    }
    public static String largeMessage() {
        return lorem.substring(0,2000);
    }
    public static void logRequest(SerializableRequest request, Logger logger) {
        logger.info(parseRequestToString(request));
    }
    public static void logAnswer(SerializableAnswer answer, Logger logger) {
        logger.info(parseAnswerToString(answer));
    }
    private static String parseRequestToString(SerializableRequest request) {
        String type;
        switch (request.type) {
            case CREATECLIENT:
                type = "CreateClient";
                break;
            case CREATEQUEUE:
                type = "CreateQueue";
                break;
            case DELETEQUEUE:
                type = "DeleteQueue";
                break;
            case QUERYQUEUESFORRECEIVER:
                type = "QueryQueuesForReceiver";
                break;
            case SENDMESSAGETOALL:
                type = "SendMessageToAll";
                break;
            case SENDMESSAGETORECEIVER:
                type = "SendMessageToReceiver";
                break;
            case POPQUEUE:
                type = "PopQueue";
                break;
            case PEEKQUEUE:
                type = "PeekQueue";
                break;
            case QUERYMESSAGESFORSENDER:
                type = "QueryMessagesForSender";
                break;
            case QUERYMESSAGESFORRECEIVER:
                type = "QueryMessagesForReceiver";
                break;
            default:
                type = "ERROR";
        }
        if (request.getMessage().length() == 200) {
            return type + delimiter + String.valueOf(request.getId()) + delimiter + String.valueOf(request.getSource()) + delimiter + String.valueOf(request.getTarget()) + delimiter + String.valueOf(request.getQueue()) + delimiter + "smallMessage";
        } else if (request.getMessage().length() == 1100) {
            return type + delimiter + String.valueOf(request.getId()) + delimiter + String.valueOf(request.getSource()) + delimiter + String.valueOf(request.getTarget()) + delimiter + String.valueOf(request.getQueue()) + delimiter + "mediumMessage";
        } else if (request.getMessage().length() == 2000) {
            return type + delimiter + String.valueOf(request.getId()) + delimiter + String.valueOf(request.getSource()) + delimiter + String.valueOf(request.getTarget()) + delimiter + String.valueOf(request.getQueue()) + delimiter + "largeMessage";
        } else {
            return type + delimiter + String.valueOf(request.getId()) + delimiter + String.valueOf(request.getSource()) + delimiter + String.valueOf(request.getTarget()) + delimiter + String.valueOf(request.getQueue()) + delimiter + request.getMessage();
        }
    }
    private static String parseAnswerToString(SerializableAnswer answer) {
        String type;
        switch (answer.type) {
            case ACK:
                type = "Ack";
                break;
            case ERROR:
                type = "Error";
                break;
            case ANSWERMESSAGE:
                type = "Message";
                break;
            case ANSWERQUEUE:
                type = "Queue";
                break;
            default:
                type = "ERROR";
        }
        if(answer.getMessage().length()==200) {
            return type+delimiter+String.valueOf(answer.getRequestId())+delimiter+String.valueOf(answer.getResultId())+delimiter+"smallMessage";
        } else if (answer.getMessage().length()==1100) {
            return type+delimiter+String.valueOf(answer.getRequestId())+delimiter+String.valueOf(answer.getResultId())+delimiter+"mediumMessage";
        } else if (answer.getMessage().length()==2000) {
            return type+delimiter+String.valueOf(answer.getRequestId())+delimiter+String.valueOf(answer.getResultId())+delimiter+"largeMessage";
        } else {
            return type+delimiter+String.valueOf(answer.getRequestId())+delimiter+String.valueOf(answer.getResultId())+delimiter+answer.getMessage();
        }

    }
    private static String delimiter = "|";
    private static String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor. Praesent et diam eget libero egestas mattis sit amet vitae augue. Nam tincidunt congue enim, ut porta lorem lacinia consectetur. Donec ut libero sed arcu vehicula ultricies a non tortor. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean ut gravida lorem. Ut turpis felis, pulvinar a semper sed, adipiscing id dolor. Pellentesque auctor nisi id magna consequat sagittis. Curabitur dapibus enim sit amet elit pharetra tincidunt feugiat nisl imperdiet. Ut convallis libero in urna ultrices accumsan. Donec sed odio eros. Donec viverra mi quis quam pulvinar at malesuada arcu rhoncus. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. In rutrum accumsan ultricies. Mauris vitae nisi at sem facilisis semper ac in est. Vivamus fermentum semper porta. Nunc diam velit, adipiscing ut tristique vitae, sagittis vel odio. Maecenas convallis ullamcorper ultricies. Curabitur ornare, ligula semper consectetur sagittis, nisi diam iaculis velit, id fringilla sem nunc vel mi. Nam dictum, odio nec pretium volutpat, arcu ante placerat erat, non tristique elit urna et turpis. Quisque mi metus, ornare sit amet fermentum et, tincidunt et orci. Fusce eget orci a orci congue vestibulum. Ut dolor diam, elementum et vestibulum eu, porttitor vel elit. Curabitur venenatis pulvinar tellus gravida ornare. Sed et erat faucibus nunc euismod ultricies ut id justo. Nullam cursus suscipit nisi, et ultrices justo sodales nec. Fusce venenatis facilisis lectus ac semper. Aliquam at massa ipsum. Quisque bibendum purus convallis nulla ultrices ultricies. Nullam aliquam, mi eu aliquam tincidunt, purus velit laoreet tortor, viverra pretium nisi quam vitae mi. Fusce vel volutpat elit. Nam sagittis nisi dui. Suspendisse lectus leo, consectetur in tempor sit amet, placerat quis neque. Etiam luctus porttitor lorem, sed suscipit est rutrum non. Curabitur lobortis nisl a enim congue semper. Aenean commodo ultrices imperdiet. Vestibulum ut justo vel sapien venenatis tincidunt. Phasellus eget dolor sit amet ipsum dapibus condimentum vitae quis lectus. Aliquam ut massa in turpis dapibus convallis. Praesent elit lacus, vestibulum at malesuada et, ornare et est. Ut augue nunc, sodales ut euismod non, adipiscing vitae orci. Mauris ut placerat justo. Mauris in ultricies enim. Quisque nec est eleifend nulla ultrices egestas quis ut quam. Donec sollicitudin lectus a mauris pulvinar id aliquam urna cursus. Cras quis ligula sem, vel elementum mi. Phasellus non ullamcorper urna. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. In euismod ultrices facilisis. Vestibulum porta sapien adipiscing augue congue id pretium lectus molestie. Proin quis dictum nisl. Morbi id quam sapien, sed vestibulum sem. Duis elementum rutrum mauris sed convallis. Proin vestibulum magna mi. Aenean tristique hendrerit magna, ac facilisis nulla hendrerit ut. Sed non tortor sodales quam auctor elementum. Donec hendrerit nunc eget elit pharetra pulvinar. Suspendisse id tempus tortor. Aenean luctus, elit commodo laoreet commodo, justo nisi consequat massa, sed vulputate quam urna quis eros. Donec vel.";

    @Deprecated
    public static int answerBufferSize() {
        // setup dummy message to get max size of buffer

        SerializableAnswer message = new SerializableAnswer(SerializableAnswer.AnswerType.ACK,-1, -1, -1, MessagePassingProtocol.largeMessage());

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();


        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteOutputStream.toByteArray().length;
    }

    @Deprecated
    public static int requestBufferSize() {
        // setup dummy message to get max size of buffer

        SerializableRequest message = new SerializableRequest(SerializableRequest.RequestType.SENDMESSAGETOALL, -1, -1, -1, -1, MessagePassingProtocol.largeMessage());

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();


        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteOutputStream.toByteArray().length;
    }

    public static SerializableRequest parseRequest(byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        ObjectInputStream obj = null;

        try {
            obj = new ObjectInputStream(new ByteArrayInputStream(dataCopy));
        } catch (IOException e) {
            e.printStackTrace();
        }
        SerializableRequest request = null;
        try {
            request = (SerializableRequest)obj.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return request;
    }
    public static SerializableAnswer parseAnswer(byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        ObjectInputStream obj = null;

        try {
            obj = new ObjectInputStream(new ByteArrayInputStream(dataCopy));
        } catch (IOException e) {
            e.printStackTrace();
        }
        SerializableAnswer answer = null;
        try {
            answer = (SerializableAnswer)obj.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return answer;
    }
}
