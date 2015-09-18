package io.enforcer.xwing;

import io.enforcer.deathstar.DeathStarClient;
import io.enforcer.deathstar.pojos.Status;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by herret2 on 8/13/2015.
 */
public class StatusMaster {

    /**
     * Logger to be used in class
     */
    private static final Logger logger = Logger.getLogger(StatusMaster.class.getName());

    /**
     * Configuration properties
     */
    private XWingConfiguration config;

    /**
     * Connection to death star
     */
    private DeathStarClient deathstar;

    /**
     * Period of time in seconds between heartbeats
     */
    private int heartbeat;

    /**
     * The process id of the x-wing, retrieved from config
     */
    private int xwingid;

    /**
     * The hostname of this x-wing, retrieved from config
     */
    private String xwinghost;

    /**
     * The periodic status publishing task is run by this scheduler
     */
    private ScheduledExecutorService scheduler;

    /**
     * @param heartBeatTime     Period of time in seconds between heartbeats
     * @param deathStarClient   Connection to death star
     */
    public StatusMaster (int heartBeatTime, DeathStarClient deathStarClient) {
        heartbeat = heartBeatTime;
        deathstar = deathStarClient;
        config = XWing.getConfig();
        xwingid = Integer.parseInt(config.getProperty("processId"));
        xwinghost = config.getProperty("hostname");
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Allow the X-Wing to publicly start the Status sending thread
     */
    public void startStatusSending() {
        scheduler.scheduleAtFixedRate(new StatusPublisherThread(), 5, heartbeat, TimeUnit.SECONDS);
    }

    /**
     * Thread responsible for sending heartbeat/status to death star
     */
    private class StatusPublisherThread implements Runnable {
        @Override
        public void run() {
            Instant now = Instant.now();
            Status status = new Status(xwingid, xwinghost, now.toString());
            logger.log(Level.FINE, "Sending status: {0}", status);
            deathstar.sendStatus(status);
        }
    }

}
