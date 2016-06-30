package io.enforcer.vader;

import io.enforcer.deathstar.DeathStarClient;
import io.enforcer.deathstar.pojos.MetricRequest;

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
    private MetricRequest req;

    public Vader (MetricRequest req) {
        deathstar = connectToDeathStar();

        if (deathstar != null) {
            this.req = req;
            graphiteMaster = new GraphiteMaster(req);
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

    public GraphiteMaster getGraphiteMaster() { return this.graphiteMaster; }

}
