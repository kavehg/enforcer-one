package io.enforcer.vader;

import io.enforcer.deathstar.DeathStarClient;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by SCHIAJ2 on 6/23/2016.
 */

/**
 * Vader - Early version of a graphite metrics monitoring component
 */
public class Vader {

    private static final Logger logger = Logger.getLogger(Vader.class.getName());
    private static final VaderConfiguration config = new VaderConfiguration(null);
    private static final ConsoleHandler consoleHandler = new ConsoleHandler();

    private DeathStarClient deathstar;
    private GraphiteMaster graphiteMaster;

    public static void main (String[] args) {
        Logger globalLogger = Logger.getLogger("");
        Handler[] handlers = globalLogger.getHandlers();
        for(Handler handler : handlers) {
            globalLogger.removeHandler(handler);
        }

        // Configure console logger & set levels
        consoleHandler.setLevel(Level.INFO);
        globalLogger.addHandler(consoleHandler);
        globalLogger.setLevel(Level.ALL);

        new Vader(args[0]);
    }

    public Vader (String jsonString) {

        deathstar = connectToDeathStar();

        if (deathstar != null) {
            graphiteMaster = new GraphiteMaster(jsonString);
            graphiteMaster.startMetricMonitoring();
        } else {
            logger.log(Level.SEVERE, "Could not connect ot DeathStar");
        }
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
            logger.log(Level.INFO, "Vader: Connection to deathstar established");
            return new DeathStarClient(deathStarHost, deathStarPortInt);

        }
    }

    public static VaderConfiguration getConfig() {
        return config;
    }

}
