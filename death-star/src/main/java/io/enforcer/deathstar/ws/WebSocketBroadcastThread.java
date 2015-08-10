package io.enforcer.deathstar.ws;

import io.enforcer.deathstar.DeathStar;
import io.enforcer.deathstar.pojos.Report;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The broadcast thread is responsible for pushing received report
 * events to all web clients for the purpose of updating the dashboard
 */
public class WebSocketBroadcastThread implements Runnable {

    /**
     * The broadcast thread publishes events placed on this queue
     */
    private final ArrayBlockingQueue<Report> broadcastQueue;

    /**
     * class logger
     */
    private final Logger logger = Logger.getLogger(WebSocketBroadcastThread.class.getName());

    /**
     * constructor
     */
    public WebSocketBroadcastThread(ArrayBlockingQueue<Report> broadcastQueue) {
        this.broadcastQueue = broadcastQueue;
        logger.log(Level.INFO, "WebSocket broadcast thread instantiated: {0}", this);
    }

    /**
     * Takes reports placed on the broadcast queue and publishes them out to
     * websocket clients for the purpose of updating the dashboard
     */
    @Override
    public void run() {
        while (true) { // todo: sane stoppage
            try {
                logger.log(Level.INFO, "Broadcast thread waiting for update");
                Report pollResult = broadcastQueue.take();
                DeathStar.getWebSocketServer().broadcastToAllWebSocketClients(pollResult.toString()); // todo json
                logger.log(Level.INFO, "Report event published to websocket clients: ");
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Broadcast queue thread got interrupted", e);
            }
        }
    }
}