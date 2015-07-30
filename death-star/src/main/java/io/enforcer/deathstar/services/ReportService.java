package io.enforcer.deathstar.services;

import io.enforcer.deathstar.pojos.Report;

import java.util.HashSet;
import java.util.Set;
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
     * received
     */
    private final Set<Report> reportStore;

    /**
     * Constructor
     */
    public ReportService() {
        reportStore = new HashSet<>();
    }

    /**
     * Add a report to the report service
     *
     * @param report to be added
     */
    public void addReport(Report report) {
        logger.log(Level.INFO, "received a report from an x-wing: {0}", report);
        reportStore.add(report);
    }

    /**
     * Returns the number of reports that have been received
     *
     * @return number of received reports
     */
    public Integer numberOfReceivedReports() {
        return reportStore.size();
    }

}
