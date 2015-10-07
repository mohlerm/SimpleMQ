package ch.mohlerm.distributor;

import ch.mohlerm.config.Config;
import ch.mohlerm.worker.Worker;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by marcel on 10/5/15.
 */

public class Distributor implements Runnable {
    private static Logger log = Logger.getLogger(Distributor.class.getName());
    // number of connected clients
    private volatile int nrOfClients = 0;
    // The selector we'll be monitoring
    private Selector selector;
    // The channel on which we'll accept connections
    private ServerSocketChannel serverChannel;

    private LinkedBlockingQueue<Worker> workerQueue;


    public Distributor() throws IOException{
        int cpus = Runtime.getRuntime().availableProcessors();
        workerQueue = new LinkedBlockingQueue<>();
        for (int i = 0; i < Config.CPUWORKERSCALING * cpus; i++) {
            workerQueue.add(new Worker());
        }
        log.debug("Initialized " + workerQueue.size() + " workers");
        this.selector = this.initSelector();
        //  distributor.setSelector(selector);
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public void run() {
        log.debug("Distributor started");
        boolean listening = true;
        while (listening) {
            try {
                // Wait for an event one of the registered channels
                this.selector.select();

                // Iterate over the set of keys for which events are available
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        log.debug("Valid key");
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isAcceptable()) {
                        log.debug("Acceptable key");
                        this.accept(key);
                    } else if (key.isReadable()) {
                        log.debug("Readable key");
                        this.read(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void accept(SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        Worker worker = null;
        try {
            worker = workerQueue.take();
            worker.setChannel(socketChannel);
            worker.setKey(key);
            worker.setCallBack(this);
            Thread t = new Thread(worker);
            t.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug("Hand over job to worker");


    }


    private Selector initSelector() throws IOException {
        // Create a new selector
        Selector socketSelector = SelectorProvider.provider().openSelector();

        // Create a new non-blocking server socket channel
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // Bind the server socket to the specified address and port
        serverChannel.socket().bind(new InetSocketAddress(Config.SERVERPORT));

        // Register the server socket channel, indicating an interest in
        // accepting new connections
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }

    public void workerCallBack(Worker worker) {
        try {
            {
                workerQueue.put(worker);
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug("WorkerQueuesize: " + workerQueue.size());
    }

}
