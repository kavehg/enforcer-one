package io.enforcer.vader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by SCHIAJ2 on 6/23/2016.
 */
public class VaderConfiguration {

    private static final Logger logger = Logger.getLogger(VaderConfiguration.class.getName());

    private static final String configFileName = "config.properties";

    private Properties properties;

    VaderConfiguration(Properties properties) {
        this.properties = new Properties();
        if (properties == null){
            loadConfigurations();
        } else {
            this.properties.putAll(properties);
        }

        Properties systemProperties = System.getProperties();
        this.properties.putAll(systemProperties);
    }

    /**
     * Loads properties from config file
     */
    private void loadConfigurations() {

        InputStream inputStream = Vader.class.getClassLoader().getResourceAsStream(configFileName);
        if (inputStream == null) {
            logger.log(Level.SEVERE, "Could not load config file: {0}", configFileName);
        }

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not load properties from input stream");
        }

    }

    public String getProperty(String key) { return this.properties.getProperty(key);}
}
