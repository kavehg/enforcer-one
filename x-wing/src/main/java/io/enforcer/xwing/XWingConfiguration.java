package io.enforcer.xwing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kavehg on 7/14/2015.
 */
public class XWingConfiguration {

    /**
     * Logger to be used in class
     */
    private static final Logger logger = Logger.getLogger(XWingConfiguration.class.getName());

    /**
     * This config file should be on the classpath and contains
     * various configuration parameters
     */
    private static final String configFileName = "config.properties";

    /**
     * Properties loaded from configuration file
     */
    private Properties properties;

    /**
     * Can initialize the config by passing specific properties or
     * by passing null which will result in an attempt to load from
     * a file on classpath
     *
     * @param properties specific properties to use
     */
    public XWingConfiguration(Properties properties) {
        this.properties = new Properties();

        // Either load properties or use the provided ones
        if(properties == null)
            loadConfiguration();
        else
            this.properties.putAll(properties);

        // Add system properties
        Properties systemProperties = System.getProperties();
        this.properties.putAll(systemProperties);
    }

    /**
     * Retrieve the value of a given property
     *
     * @param key to search for
     * @return value of provided key
     */
    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    /**
     * Loads properties from the config file
     */
    private void loadConfiguration() {
        InputStream inputStream = null;

        inputStream = ProcessMaster.class.getClassLoader().getResourceAsStream(configFileName);
        if(inputStream == null)
            logger.log(Level.SEVERE, "could not load config file: {0}", configFileName);

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "could not load properties from input stream", e);
        }
    }

    /**
     * @return String representation of the properties loaded from config file
     */
    private String dumpConfiguration() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for(String key : properties.stringPropertyNames()) {
            sb.append("\t");
            sb.append(key);
            sb.append(" : ");
            sb.append(properties.getProperty(key));
        }
        return sb.toString();
    }
}
