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
     * Logger
     */
    private static final Logger logger = Logger.getLogger(ReportService.class.getName());

    private static final Set<Report> reports;

    static {
        reports = new HashSet<>();
    }

    public void addReport(Report report) {
        logger.log(Level.INFO, "received a report from an x-wing: {0}", report);
        reports.add(report);
    }

}
