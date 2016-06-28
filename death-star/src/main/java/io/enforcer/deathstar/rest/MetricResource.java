package io.enforcer.deathstar.rest;

import io.enforcer.deathstar.DeathStar;
import io.enforcer.deathstar.pojos.Metric;
import io.enforcer.deathstar.services.MetricService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by SCHIAJ2 on 6/27/2016.
 */
@Path("metrics")
public class MetricResource {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(MetricResource.class.getName());

    /**
     * Reference to metric store
     */
    private MetricService metricService;

    /**
     * Instantiate REST resource
     */
    public MetricResource() {
        this.metricService = DeathStar.getMetricService();
        logger.log(Level.FINE, "Metric service instatiated: {0}", this);
    }

    @POST
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces (MediaType.APPLICATION_JSON)
    public void roundTrip (Metric metric) {
        logger.log(Level.INFO, "Metric received: {0}", metric);
        metricService.addMetric(metric);
    }

}
