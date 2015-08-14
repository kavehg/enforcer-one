package io.enforcer.xwing;

import io.enforcer.deathstar.DeathStarClient;
import io.enforcer.deathstar.pojos.Report;
import io.enforcer.deathstar.pojos.Status;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by herret2 on 8/13/2015.
 */
public class StatusMaster implements Runnable {

    /**
     * Logger to be used in class
     */
    private static final Logger logger = Logger.getLogger(ProcessMaster.class.getName());

    /**
     * Configuration properties
     */
    private XWingConfiguration config;

    private DeathStarClient deathstar;
    private int heartbeat;
    private int xwingid;
    private String xwinghost;
    private volatile boolean alive;

    public StatusMaster (int heartBeatTime, int xWingId, String host, DeathStarClient deathStarClient) {

        heartbeat = heartBeatTime;
        deathstar = deathStarClient;
        xwingid = xWingId;
        xwinghost = host;
        alive = false;

    }

    /*
     * Allow the X-Wing to publicly start the Status sending thread
     */
    public void startStatusSending() {

        new Thread(){
            public void run(){
                while (alive) {

                    try {
                        sendStatus();
                        Thread.sleep(heartbeat);
                    } catch (Exception e)
                    {
                        logger.log(Level.SEVERE, "StatusMaster Thread error: " + e.getMessage());
                    }
                }
            }
        }.start();

    }

    public void run() {

    }

    /*
     * Stop the sending of statuses
     */
    public void stopStatusSending() {
        alive = false;
    }


    /*
     * Sends a status via the deathstar client
     */
    private void sendStatus () {

        Status status = buildStatus();
        deathstar.sendStatus(status);
    }

    /*
     * Builds a status with proper timestamp
     */
    private Status buildStatus() {
        long ms = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date resultdate = new Date(ms);
        String date = sdf.format(resultdate);

        return new Status(xwingid, xwinghost, date);
    }
}
