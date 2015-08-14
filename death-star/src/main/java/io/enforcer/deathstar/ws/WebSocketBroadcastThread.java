package io.enforcer.deathstar.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.enforcer.deathstar.DeathStar;
import io.enforcer.deathstar.pojos.Report;
import io.enforcer.deathstar.pojos.Status;

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
    private final ArrayBlockingQueue<Report> reportBroadcastQueue;

    /**
     * The broadcast thread publishes events placed on this queue
     */
    private final ArrayBlockingQueue<Status> statusBroadcastQueue;

    /**
     * class logger
     */
    private final Logger logger = Logger.getLogger(WebSocketBroadcastThread.class.getName());

    /**
     * Jackson json serializer
     */
    private final ObjectMapper jsonMapper;

    /**
     * constructors
     */
    public WebSocketBroadcastThread(ArrayBlockingQueue<Report> broadcastQueue) {
        this.reportBroadcastQueue = broadcastQueue;
        this.statusBroadcastQueue = null;
        this.jsonMapper = new ObjectMapper();
        logger.log(Level.FINE, "WebSocket broadcast thread instantiated: {0}", this);
    }

    public WebSocketBroadcastThread(ArrayBlockingQueue<Status> broadcastQueue, int n) {
        this.statusBroadcastQueue = broadcastQueue;
        this.reportBroadcastQueue = null;
        this.jsonMapper = new ObjectMapper();
        logger.log(Level.FINE, "WebSocket broadcast thread instantiated: {0}", this);
    }

    /**
     * Takes reports placed on the broadcast queue and publishes them out to
     * websocket clients for the purpose of updating the dashboard
     */
    @Override
    public void run() {
        while (true) { // todo: sane stoppage
            try {
                logger.log(Level.FINE, "Broadcast thread waiting for update");
                if (statusBroadcastQueue != null)
                {
                    Status statusToBroadcast = statusBroadcastQueue.take();
                    String jsonString = jsonMapper.writeValueAsString(statusToBroadcast);
                    DeathStar.getWebSocketServer().broadcastToAllWebSocketClients(jsonString);
                    logger.log(Level.FINE, "Broadcast status thread published an event to all webSocket clients: {0}", jsonString);
                    statusBroadcastQueue.clear();
                }
                else
                {
                    Report reportToBroadcast = reportBroadcastQueue.take();
                    String jsonString = jsonMapper.writeValueAsString(reportToBroadcast);
                    DeathStar.getWebSocketServer().broadcastToAllWebSocketClients(jsonString);
                    logger.log(Level.FINE, "Broadcast report thread published an event to all webSocket clients: {0}", jsonString);
                    reportBroadcastQueue.clear();
                }

            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "WebSocket broadcast thread got interrupted", e);
            } catch (JsonProcessingException e) {
                logger.log(Level.WARNING, "Problems converting report to json", e);
            }
        }
    }
}