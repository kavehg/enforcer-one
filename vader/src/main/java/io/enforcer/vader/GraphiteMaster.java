package io.enforcer.vader;

import com.google.gson.*;
import io.enforcer.deathstar.DeathStarClient;
import io.enforcer.deathstar.pojos.Metric;
import io.enforcer.deathstar.pojos.Report;
import io.enforcer.vader.pojos.MetricRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by SCHIAJ2 on 6/23/2016.
 */
public class GraphiteMaster {

    /**
     * Logger for class
     */
    private static final Logger logger = Logger.getLogger(GraphiteMaster.class.getName());

    /**
     * DeathStarClient for death star api
     */
    private DeathStarClient deathstar;

    /**
     * configuration properties
     */
    private VaderConfiguration config = Vader.getConfig();

    /**
     * Json parser and features
     */
    private Gson gson = new Gson();

    /**
     * Holds the MetricRequest pojo associated with this Vader
     */
    private MetricRequest request;

    /**
     * Holds the most recent Metric pulled in from the Graphite server
     */
    private List<Metric> currentMetrics;

    /**
     * Execution service
     */
    private ScheduledExecutorService service;

    public GraphiteMaster (String metric) {
        deathstar = connectToDeathStar();
        this.request = gson.fromJson(metric, MetricRequest.class);
    }

    /**
     * Request json data from graphite using the URL that the user
     * input from the dashboard.
     */
    private void graphiteRequest() {
        BufferedReader reader;
        try {
            URL url = new URL(request.url);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String json = reader.readLine();
            currentMetrics = parseMetricData(json);
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.INFO, "Vader: Unable to retrieve data from Graphite");
        }

    }

    /**
     * Parse json coming from graphite and allocate data to the respective
     * instance variable (Only gets the y axis data, x axis irrelevant).
     * Can also handle arrays of multiple metrics from multiple targets.
     *
     * @param json
     * @return
     */
    private List<Metric> parseMetricData(String json) {
        List<Metric> metricList = new ArrayList<>(1000);
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonArray();
        Iterator<JsonElement> iterator = array.iterator();
        while (iterator.hasNext()) {
            JsonObject obj = gson.fromJson(iterator.next(), JsonObject.class);
            Metric metric = new Metric();
            metric.target = obj.get("target").getAsString();
            JsonArray dataArray = obj.get("datapoints").getAsJsonArray();
            logger.log(Level.INFO, dataArray.toString());
            for (int i = 0; i < 5; i++) {
                JsonArray datapoint = dataArray.get(i).getAsJsonArray();
                if (!(datapoint.get(0) instanceof JsonNull)) {
                    float x = datapoint.get(0).getAsFloat();
                    metric.datapoints[i] = x;
                } else {
                    metric.datapoints[i] = -1;
                }
            }
            metricList.add(metric);
        }
        return metricList;
    }

    /**
     * Checks if the average of 5 metric datapoints is above the user specified threshold
     * or if data is null
     * @return
     */

    private int exceedsThreshold(Metric currentMetric) {
        float currentAverage = 0;
        int nullCounter = 0;
        for (float data : currentMetric.datapoints) {
            currentAverage += data;
            if (data == -1) {
                nullCounter += 1;
            }
        }
        //ToDo: Average not accurate, 5th datapoint is usually null
        currentAverage = currentAverage / ((float) 5.0 - (float) nullCounter);
        currentMetric.average = currentAverage;
        if (currentAverage > request.threshold) { return -2; }
        else if (nullCounter == 5) { return -1; }
        else { return 0; }

    }


    /**
     * Begins executing the GraphiteMonitor Runnable at a fixed rate
     */
    public void startMetricMonitoring(){
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new GraphiteMonitor(), 10, 10, TimeUnit.SECONDS);
    }

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

    /**
     * Runnable class which generates reports and sends to deathstar if the metric exceeds
     * the threshold
     */

    private class GraphiteMonitor implements Runnable {

        @Override
        public void run() {
            graphiteRequest();
            Iterator<Metric> iterator = currentMetrics.iterator();
            while (iterator.hasNext()) {
                Metric currentMetric = iterator.next();
                int result = exceedsThreshold(currentMetric);
                if (result == -2) {
                    deathstar.sendMetric(generateMetric(currentMetric));
                }
                else if (result == -1) {
                    currentMetric.average = -1;
                    deathstar.sendMetric((generateMetric(currentMetric)));
                }
            }

        }

        /**
         * Handles report generation
         * @return
         */
        private Metric generateMetric(Metric currentMetric) {
            String metricPath = currentMetric.target.substring(10, currentMetric.target.length() - 17);
            return new Metric(
                    metricPath,
                    currentMetric.datapoints,
                    currentMetric.average,
                    request.threshold,
                    LocalDateTime.now().toString(),
                    request.metricDetail,
                    "New"
            );
        }
    }
}
