package io.enforcer.deathstar.rest;

import io.enforcer.deathstar.pojos.Report;
import io.enforcer.deathstar.services.ReportService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@Path("reports")
public class ReportResource {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(ReportResource.class.getName());

    private ReportService reportService;

    public ReportResource() {
        this.reportService = new ReportService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Report getIt() {
        logger.log(Level.INFO, "getting report");

        return new Report(123, "MainClass", "STARTED", "host1", "2015-02-05T13:09:32");
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Report roundTrip(Report report) {
        logger.log(Level.INFO, "Received a report: ", report);
        reportService.addReport(report);
        return report;
    }
}
