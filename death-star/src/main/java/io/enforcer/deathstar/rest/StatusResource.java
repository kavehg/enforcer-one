package io.enforcer.deathstar.rest;

import io.enforcer.deathstar.DeathStar;
import io.enforcer.deathstar.pojos.Status;
import io.enforcer.deathstar.services.StatusService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kavehg on 2/24/2015.
 */
@Path("status")
public class StatusResource {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(StatusResource.class.getName());

    /**
     * Reference to the status store
     */
    private StatusService statusService;

    /**
     * Instantiate REST resource for statuses
     */
    public StatusResource() {
        this.statusService = DeathStar.getStatusService();
        logger.log(Level.FINE, "Status resource instantiated: {0}", this);
    }

    /**
     *
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Status getIt() {

        return new Status(123, "host123", "2012-02-12T23:45:32Z");
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Status roundTrip(Status status) {
        logger.log(Level.INFO, "Recieved status update: {0}", status);
        statusService.addStatusUpdate(status);
        return status;
    }

}
