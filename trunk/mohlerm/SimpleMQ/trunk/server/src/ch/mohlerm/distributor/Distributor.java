package ch.mohlerm.distributor;

import ch.mohlerm.config.Config;
import ch.mohlerm.initializer.Connection;
import ch.mohlerm.worker.Worker;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by marcel on 10/5/15.
 */
public class Distributor implements Runnable {

    LinkedList<Connection> connectionList;
    LinkedBlockingQueue<Worker> workerQueue;


    public Distributor() {
        connectionList = new LinkedList<>();
        workerQueue = new LinkedBlockingQueue<>();
        int cpus = Runtime.getRuntime().availableProcessors();
        for(int i = 0; i < Config.CPUWORKERSCALING*cpus;i++) {
            workerQueue.add(new Worker());
        }
    }


    @Override
    public void run() {
        while(true) {
            for(Connection connection : connectionList) {
                if(!connection.isBusy() && connection.messageWaiting()) {
                    Worker worker = null;
                    try {
                        worker = workerQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    connection.setBusy(true);
                    worker.setConnection(connection);
                    worker.setCallBack(this);
                    worker.run();
                }
            }
        }
    }
    public void addConnection(Connection connection) {
        connectionList.add(connection);
    }

    public void workerCallBack(Worker worker, Connection connection) {
        workerQueue.add(worker);
        connection.setBusy(false);
    }
}
