package io.enforcer.deathstar.services;

import io.enforcer.deathstar.DeathStar;
import io.enforcer.deathstar.pojos.Action;
import io.enforcer.deathstar.pojos.Report;
import io.enforcer.deathstar.ws.WebSocketServer;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
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
     * Constructor
     */
    public ReportService() {
        reportStore = new ConcurrentHashMap<>();
        webSocketServer = DeathStar.getWebSocketServer();
        broadcastQueue = new ArrayBlockingQueue<>(1000);
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
        // add report to broadcast queue
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

}
