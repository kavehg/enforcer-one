package io.enforcer.deathstar.services;

import io.enforcer.deathstar.DeathStar;
import io.enforcer.deathstar.pojos.Action;
import io.enforcer.deathstar.pojos.Report;
import io.enforcer.deathstar.ws.WebSocketServer;

import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kavehg on 2/24/2015.
 */
public class ReportService {

    /**
     * Class logger
     */
    private static final Logger logger = Logger.getLogger(ReportService.class.getName());

    /**
     * The report store is a collection of all reports that have been
     * received.
     *
     * The value of the map contains all actions that have been taken
     * for the report specified in the key of the map.
     */
    private final ConcurrentHashMap<Report, ConcurrentLinkedDeque<Action>> reportStore;

    /**
     * Hold a reference to the websocket server in order to broadcast events
     * to all web clients
     */
    private final WebSocketServer webSocketServer;

    /**
     * Reports that are meant to be broadcast to all http/websocket
     * clients are placed on this queue and picked up by the publishing
     * thread.
     */
    private final ArrayBlockingQueue<Report> broadcastQueue;

    /**
     * The periodic monitoring task is run by this scheduler
     */
    private ExecutorService broadcastExecutor;

    /**
     * Constructor
     */
    public ReportService() {
        reportStore = new ConcurrentHashMap<>();
        webSocketServer = DeathStar.getWebSocketServer();
        broadcastQueue = new ArrayBlockingQueue<>(1000);
        broadcastExecutor = Executors.newSingleThreadExecutor();
        logger.log(Level.FINE, "ReportService instantiated: {0}", this);
    }

    /**
     * Starts the broadcast thread
     */
    public void startBroadcastThread() {
        broadcastExecutor.execute(new WebsocketBrodacastThread());
        logger.log(Level.INFO, "Websocket broadcast executor started");
    }

    /**
     * Stops the broadcast thread
     */
    private void stopBroadcastThread() {
        broadcastExecutor.shutdown();
        logger.log(Level.INFO, "Websocket broadcast thread stopped");
    }

    /**
     * Add a report to the report service
     *
     * @param report to be added
     * @return null if report is successfully added or associated value if report already exists
     */
    public ConcurrentLinkedDeque<Action> addReport(Report report) {
        logger.log(Level.INFO, "storing report: {0}", report);

        ConcurrentLinkedDeque<Action> existingValueForReport = reportStore.putIfAbsent(report, new ConcurrentLinkedDeque<>());
        if(existingValueForReport != null) {
            logger.log(Level.INFO, "This report was already present in the store and is being ignored: {0}", report);
            return existingValueForReport;
        }
        // add report to broadcast queue where it will be picked up by
        // the WebsocketBroadcastThread
        broadcastQueue.add(report);
        return null;
    }

    /**
     * Store a batch of reports in the service
     *
     * @param reports collection of reports
     */
    public void addReports(Set<Report> reports) {
        logger.log(Level.INFO, "storing a batch of {0} reports", reports.size());
        reports.forEach(this::addReport);
    }

    /**
     * Returns the number of reports that have been received
     *
     * @return number of received reports
     */
    public Integer numberOfReceivedReports() {
        return reportStore.size();
    }

    /**
     * Returns all reports that have been received
     *
     * @return set of all received reports
     */
    public Set<Report> getAllReports() {
        return reportStore.keySet();
    }

    /**
     * The broadcast thread is responsible for pushing received report
     * events to all web clients for the purpose of updating the dashboard
     */
    private class WebsocketBrodacastThread implements Runnable {

        /**
         * class logger
         */
        private final Logger logger = Logger.getLogger(WebsocketBrodacastThread.class.getName());

        /**
         * constructor
         */
        public WebsocketBrodacastThread() {
            logger.log(Level.INFO, "Websocket broadcast thread started");
        }

        /**
         * Takes reports placed on the broadcast queue and publishes them out to
         * websocket clients for the purpose of updating the dashboard
         */
        @Override
        public void run() {
            logger.log(Level.FINE, "Broadcast thread waiting for update");

            try {
                Report pollResult = broadcastQueue.take();
                webSocketServer.broadcastToAllWebSocketClients(pollResult.toString()); // todo json
                logger.log(Level.FINE, "Report event published to websocket clients: ");
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Broadcast queue thread got interrupted", e);
            }

        }
    }
}
