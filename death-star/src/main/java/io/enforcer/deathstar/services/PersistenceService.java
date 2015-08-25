package io.enforcer.deathstar.services;

import io.enforcer.deathstar.pojos.Action;
import io.enforcer.deathstar.pojos.Report;
import io.enforcer.deathstar.ws.WebSocketBroadcastThread;

import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by herret2 on 8/24/2015.
 */

public class PersistenceService {

    /**
     * Class logger
     */
    private static final Logger logger = Logger.getLogger(PersistenceService.class.getName());

    /**
     * The persistence store is a collection of all reports that have been
     * received.
     *
     * The value of the map contains all actions that have been taken
     * for the report specified in the key of the map.
     */
    private final ConcurrentHashMap<Report, ConcurrentLinkedDeque<Action>> reportStore;

    /**
     * Reports that are meant to be broadcast to all http/websocket
     * clients are placed on this queue and picked up by the publishing
     * thread.
     */
    private final ArrayBlockingQueue<Report> reportBroadcastQueue;

    /**
     * The periodic monitoring task is run by this scheduler
     */
    private ExecutorService reportBroadcastExecutor;

    /**
     * Constructor
     */
    public PersistenceService() {
        reportStore = new ConcurrentHashMap<>();
        reportBroadcastQueue = new ArrayBlockingQueue<>(1000);
        reportBroadcastExecutor = Executors.newSingleThreadExecutor();
        logger.log(Level.FINE, "ReportService instantiated: {0}", this);
    }

    /**
     * Starts the broadcast thread
     */
    public void startBroadcastThread() {
        reportBroadcastExecutor.execute(new WebSocketBroadcastThread(reportBroadcastQueue));
        logger.log(Level.INFO, "WebSocket broadcast executor started");
    }

    /**
     * Stops the broadcast thread
     */
    private void stopBroadcastThread() {
        reportBroadcastExecutor.shutdown();
        logger.log(Level.INFO, "WebSocket broadcast thread stopped");
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
        // the WebSocketBroadcastThread
        reportBroadcastQueue.add(report);
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

}
