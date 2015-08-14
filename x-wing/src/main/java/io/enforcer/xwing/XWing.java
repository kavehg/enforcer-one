package io.enforcer.xwing;

import io.enforcer.deathstar.DeathStarClient;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * XWing - a lightweight java process monitor
 */
public class XWing {

    private static final Logger logger = Logger.getLogger(XWing.class.getName());
    private static final ConsoleHandler consoleHandler = new ConsoleHandler();
    private static final XWingConfiguration config = new XWingConfiguration(null);
    private static final int HEART_BEAT_TIME = 1000;

    private DeathStarClient deathstar;

    private ProcessMaster processMaster;
    private StatusMaster statusMaster;

    public static void main(String[] args) {
        // Remove default loggers
        Logger globalLogger = Logger.getLogger("");
        Handler[] handlers = globalLogger.getHandlers();
        for(Handler handler : handlers) {
            globalLogger.removeHandler(handler);
        }

        // configure console logger & set levels
        consoleHandler.setLevel(Level.INFO);
        globalLogger.addHandler(consoleHandler);
        globalLogger.setLevel(Level.ALL);

        // start
        XWing xWing = new XWing();
        //(new Thread(xWing)).start();
        xWing.run();
    }

    public XWing() {

        // setup deathstarclient
        deathstar = connectToDeathStar();

        if (deathstar != null) {
            // start the process master and monitor
            processMaster = new ProcessMaster();
            processMaster.startProcessMonitoring();

            //start the status master
            statusMaster = new StatusMaster(HEART_BEAT_TIME, 100, "localhost", deathstar);
            statusMaster.startStatusSending();
        }
    }

    private void run() {

        processMaster.logCurrentState();

    }

    /**
     * Retrieves death star connection details from configuration and
     * returns an instance of the DeathStarClient
     *
     * @return death star connection
     */
    private DeathStarClient connectToDeathStar() {
        String deathStarHost = config.getProperty("deathStarHost");
        String deathStarPort = config.getProperty("deathStarPort");
        Integer deathStarPortInt = null;

        //todo: send error messages to client
        if(deathStarHost == null) {
            logger.log(Level.SEVERE, "Could not find deathStarHost in the configuration");
            return null;
        }

        if(deathStarPort == null) {
            logger.log(Level.SEVERE, "Could not find deathStarPort in the configuration");
            return null;
        } else {
            deathStarPortInt = Integer.parseInt(deathStarPort);
            return new DeathStarClient(deathStarHost, deathStarPortInt);
        }
    }



    public static XWingConfiguration getConfig() {
        return config;
    }
}

