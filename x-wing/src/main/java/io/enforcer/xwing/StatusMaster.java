package io.enforcer.xwing;

import io.enforcer.deathstar.DeathStarClient;
import io.enforcer.deathstar.pojos.Report;
import io.enforcer.deathstar.pojos.Status;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
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

    private DeathStarClient deathstar;
    private int heartbeat;
    private int xwingid;
    private String xwinghost;

    /**
     * The periodic status publishing task is run by this scheduler
     */
    private ScheduledExecutorService scheduler;

    /**
     *
     * @param heartBeatTime
     * @param xWingId
     * @param host
     * @param deathStarClient
     */
    public StatusMaster (int heartBeatTime, int xWingId, String host, DeathStarClient deathStarClient) {
        heartbeat = heartBeatTime;
        deathstar = deathStarClient;
        xwingid = xWingId;
        xwinghost = host;
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Allow the X-Wing to publicly start the Status sending thread
     */
    public void startStatusSending() {
        scheduler.scheduleAtFixedRate(new StatusPublisherThread(), 5, 5, TimeUnit.SECONDS);
    }

    /**
     * Thread responsible for sending heartbeat/status to death star
     */
    private class StatusPublisherThread implements Runnable {
        @Override
        public void run() {
            Instant now = Instant.now();
            Status status = new Status(xwingid, xwinghost, now.toString());
            logger.log(Level.INFO, "Sending status: {0}", status);
            deathstar.sendStatus(status);
        }
    }

}
