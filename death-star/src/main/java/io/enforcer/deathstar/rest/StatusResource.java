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

    private StatusService statusService;

    public StatusResource() {
        this.statusService = DeathStar.getStatusService();
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
    public Status roundTrip(Status s) {
        logger.log(Level.INFO, "received status update: {0}", s);
        statusService.addStatusUpdate(s);
        return s;
    }
}
