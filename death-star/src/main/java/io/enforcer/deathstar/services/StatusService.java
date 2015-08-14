package io.enforcer.deathstar.services;

import io.enforcer.deathstar.pojos.Report;
import io.enforcer.deathstar.pojos.Status;
import io.enforcer.deathstar.ws.WebSocketBroadcastThread;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kavehg on 2/24/2015.
 */
public class StatusService {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(StatusService.class.getName());

    /**
     * The status store is a collection of all statuses we have received.
     * The map contains the hostname as the key and the corresponding
     * status as the value. We only care about the latest update and
     * thus no history is kept.
     */
    private final Map<String, Status> statusStore;

    /**
     * Any host that fails to check in within the allowed quiet period
     * is considered delinquent and is kept in this map. The value
     * of the map entry represents the last time a status update was
     * received.
     */
    private final Map<String, Instant> delinquentHosts;

    /**
     * If an X-wing does not check in beyond this time period, we escalate
     */
    private long allowedQuietPeriodInSeconds = 10;

    /**
     * How frequently should the status checker run
     */
    private int checkPeriodInSeconds;

    /**
     * How long should the status checker wait after it is started
     * before it starts its first run
     */
    private int initialCheckDelayInSeconds;

    /**
     * The executor responsible for running the checker thread
     */
    private final ScheduledExecutorService scheduler;

    /**
     * The periodic monitoring task is run by this scheduler
     */
    private ExecutorService statusBroadcastExecutor;

    /**
     * Reports that are meant to be broadcast to all http/websocket
     * clients are placed on this queue and picked up by the publishing
     * thread.
     */
    private final ArrayBlockingQueue<Status> statusBroadcastQueue;

    /**
     * Create an instance of the status service and initialize
     * the internal store and scheduler
     */
    public StatusService(Integer initialCheckDelayInSeconds, Integer checkPeriodInSeconds) {
        this.initialCheckDelayInSeconds = initialCheckDelayInSeconds;
        this.checkPeriodInSeconds = checkPeriodInSeconds;
        statusStore = new ConcurrentHashMap<>();
        delinquentHosts = new ConcurrentHashMap<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();

        statusBroadcastQueue = new ArrayBlockingQueue<>(1000);
        statusBroadcastExecutor = Executors.newSingleThreadExecutor();

        logger.log(Level.FINE, "status service instantiated: {0}", this);
    }

    /**
     * Starts the fixed rate status monitor thread
     */
    public void startStatusMonitoring() {
        scheduler.scheduleAtFixedRate(
                new StatusMonitor(),
                initialCheckDelayInSeconds,
                checkPeriodInSeconds,
                TimeUnit.SECONDS
        );
        logger.log(Level.INFO, "status service started monitoring");
    }

    /**
     * Stop the monitor thread
     */
    public void stopService() {
        scheduler.shutdown();
        logger.log(Level.INFO, "status service monitoring thread stopped");
    }

    /**
     * @return the time period beyond which a status update is considered late
     */
    public long getAllowedQuietPeriodInSeconds() {
        return allowedQuietPeriodInSeconds;
    }

    /**
     * @param allowedQuietPeriodInSeconds the time period beyond which a status update
     *                                    is considered late
     */
    public void setAllowedQuietPeriodInSeconds(long allowedQuietPeriodInSeconds) {
        this.allowedQuietPeriodInSeconds = allowedQuietPeriodInSeconds;
        logger.log(Level.INFO, "Allowed quiet period changed to {0}", allowedQuietPeriodInSeconds);
    }

    /**
     * @param status incoming status update from
     */
    public void addStatusUpdate(Status status) {
        logger.log(Level.INFO, "storing incoming status update {0}", status);
        Status oldStatus = statusStore.put(status.getHost(), status);
        logger.log(Level.INFO, "status stored: {0}, old status: {1}", new Object[]{status, oldStatus});
        statusBroadcastQueue.add(status);
    }

    /**
     * Returns the number of status updates that are stored in the service
     * @return number of updates in store
     */
    public Integer getNumberOfStatusUpdatesReceived() {
        return statusStore.size();
    }

    /**
     * Obtain the latest status for a given host
     * @param host for which to get the latest status
     * @return most recent status update for the supplied host
     */
    public Status getLatestStatusByHost(String host) {
        return statusStore.get(host);
    }

    /**
     * For a given host return the last time a status update was
     * received.
     *
     * @param host to check
     * @return last time a status update was received
     */
    public Instant getDelinquentHostLastUpdate(String host) {
        return delinquentHosts.get(host);
    }

    /**
     * @return How many hosts are currently considered delinquent
     */
    public Integer getNumberOfDelinquentHosts() {
        return delinquentHosts.size();
    }

    /**
     * Check to see if the given host is currently late on its status
     * update
     *
     * @param host to check for delinquency
     * @return whether this host is currently late
     */
    public Boolean isHostDelinquent(String host) {
        return delinquentHosts.containsKey(host);
    }

    /**
     * @param lateStatus this status update has been determined to be stale
     * @return the previous value for the host or null if no entry existed for host
     */
    private Instant markStatusDelinquent(Status lateStatus) {
        return delinquentHosts.put(lateStatus.getHost(), lateStatus.getTimeStampInstant());
    }

    /**
     * Determine if a given status is considered stale. Stale status
     * updates will get escalated
     *
     * @param status to be checked for staleness
     * @return whether the provided status is stale or not
     */
    private boolean statusIsStale(Status status) {
        // last reported time stamp
        Instant parsedTimeStamp = null;
        return false;
        /*try {
            logger.log(Level.INFO, "Parsed tstamp: " + Instant.parse(status.getTimeStamp()));
            parsedTimeStamp = Instant.parse(status.getTimeStamp());

        } catch (java.time.format.DateTimeParseException e) {
            logger.log(Level.WARNING, "problems parsing incoming dates", e);
            return true; // blindly say its stale since we cannot parse
        }

        Instant now = Instant.now();
        Instant latenessCutoffTime = now.minusSeconds(allowedQuietPeriodInSeconds);

        // clock skew can cause issues
        if(parsedTimeStamp.isBefore(now) && parsedTimeStamp.isAfter(latenessCutoffTime))
            return true;

        if(parsedTimeStamp.isBefore(latenessCutoffTime)) {
            String latePeriod = calculateLatePeriod(
                    LocalDateTime.ofInstant(parsedTimeStamp, ZoneId.of("Z")),
                    LocalDateTime.ofInstant(latenessCutoffTime, ZoneId.of("Z"))
            );
            logger.log(Level.INFO, "stale status: {0}, is late by: {1}", new Object[]{status, latePeriod});
            return true;
        }

        return false;*/
    }

    /**
     * Returns a string representing the difference in time period between
     * two LocalDateTime objects. Used in logs only.
     *
     * @param checkInTime start time
     * @param deadline end time
     * @return difference between start and end
     */
    private static String calculateLatePeriod(LocalDateTime checkInTime, LocalDateTime deadline) {
        LocalDateTime late = deadline
                .minusYears(checkInTime.getYear())
                .minusMonths(checkInTime.getMonthValue())
                .minusDays(checkInTime.getDayOfYear())
                .minusHours(checkInTime.getHour())
                .minusMinutes(checkInTime.getMinute())
                .minusSeconds(checkInTime.getSecond())
                .minusNanos(checkInTime.getNano());

        Period period = Period.between(deadline.toLocalDate(), checkInTime.toLocalDate());

        int years = Math.abs(period.getYears());
        int months = Math.abs(period.getMonths());
        long days = Math.abs(period.getDays());

        long hours = late.getHour();
        long minutes = late.getMinute();
        long seconds = late.getSecond();
        long milliseconds = late.getNano() / 1000;

        StringBuilder lateString = new StringBuilder();
        lateString.append(seconds);
        lateString.append("s ");
        lateString.append(milliseconds);
        lateString.append("ms");
        if(minutes > 0) {
            lateString.insert(0, "m:");
            lateString.insert(0, minutes);
        }
        if(hours > 0) {
            lateString.insert(0, "h:");
            lateString.insert(0, hours);
        }
        if(days != 0) {
            lateString.insert(0, days == 1 ? " day, " : " days, ");
            lateString.insert(0, days);
        }
        if(months != 0) {
            lateString.insert(0, months == 1 ? " month, " : " months, ");
            lateString.insert(0, months);
        }
        if(years != 0) {
            lateString.insert(0, years == 1 ? " year, " : " years, ");
            lateString.insert(0, years);
        }

        return lateString.toString();
    }

    /**
     * Starts the broadcast thread
     */
    public void startBroadcastThread() {
        statusBroadcastExecutor.execute(new WebSocketBroadcastThread(statusBroadcastQueue, 1));
        logger.log(Level.INFO, "WebSocket broadcast executor started");
    }

    /**
     * Stops the broadcast thread
     */
    private void stopBroadcastThread() {
        statusBroadcastExecutor.shutdown();
        logger.log(Level.INFO, "WebSocket broadcast thread stopped");
    }

    /**
     * Thread responsible for the periodic checking of
     * status updates, and escalation if status updates
     * are stale
     */
    private class StatusMonitor implements Runnable {
        @Override
        public void run() {
            logger.log(Level.FINE, "status checker thread run started");

            for(Status status : statusStore.values()) {
                logger.log(Level.INFO, "statusIsStale(status) = ", statusIsStale(status));
                if(statusIsStale(status)) {
                    logger.log(Level.INFO, "escalating stale status: {0}", status);
                    markStatusDelinquent(status);
                    // TODO escalate
                } else {
                    logger.log(Level.FINER, "status OK: {0}", status);
                    // add status to broadcast queue where it will be picked up by
                    // the WebSocketBroadcastThread
                    statusBroadcastQueue.add(status);
                }
            }

        }
    }

}
