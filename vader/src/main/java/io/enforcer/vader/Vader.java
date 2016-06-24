package io.enforcer.vader;

import io.enforcer.deathstar.DeathStarClient;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by SCHIAJ2 on 6/23/2016.
 */

/**
 * Vader - Very early version of a metric monitoring component,
 * metrics from graphite
 */
public class Vader {

    private static final Logger logger = Logger.getLogger(Vader.class.getName());
    private static final VaderConfiguration config = new VaderConfiguration(null);

    private DeathStarClient deathstar;
    private GraphiteMaster graphiteMaster;

    //ToDo: Make Vader a separate process from DeathStar
    //ToDo: Write tests
    public Vader (String jsonString) {

        deathstar = connectToDeathStar();
        graphiteMaster = new GraphiteMaster(jsonString);
        graphiteMaster.startMetricMonitoring();
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
