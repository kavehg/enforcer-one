package io.enforcer.deathstar.services;

import io.enforcer.deathstar.pojos.Metric;
import io.enforcer.deathstar.ws.WebSocketBroadcastThread;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by SCHIAJ2 on 6/27/2016.
 */
public class MetricService {

    private static final Logger logger = Logger.getLogger(MetricService.class.getName());

    private Map<String, Metric> metricStore;

    private ArrayBlockingQueue<Metric> metricBroadcastQueue;

    private ExecutorService metricBroadcastExecutor;

    public MetricService() {
        this.metricStore = new ConcurrentHashMap<>();
        this.metricBroadcastQueue = new ArrayBlockingQueue<Metric>(1000);
        this.metricBroadcastExecutor = Executors.newSingleThreadExecutor();

        logger.log(Level.FINE, "Metric service instantiated: {0}", this);
    }

    public void startBroadcastThread() {
        metricBroadcastExecutor.execute(new WebSocketBroadcastThread(metricBroadcastQueue, 1));
        logger.log(Level.INFO, "WebSocket broadcast executor started");
    }

    public void addMetric (Metric metric) {
        metricStore.put(metric.target, metric);
        metricBroadcastQueue.add(metric);
    }
}
