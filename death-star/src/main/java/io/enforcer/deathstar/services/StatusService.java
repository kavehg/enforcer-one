package io.enforcer.deathstar.services;

import io.enforcer.deathstar.pojos.Status;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kaveh on 2/24/2015.
 */
public class StatusService {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(StatusService.class.getName());

    /**
     * The status store is a collection of all statuses we have received.
     */
    private static final Map<String, Status> statusStore;

    /**
     * If an X-wing does not check in beyond this time period, we escalate
     */
    private static final long ALLOWED_QUIET_PERIOD_SECONDS = 10;

    /**
     * How frequently should the status checker run
     */
    private static final int CHECK_PERIOD_IN_SECONDS = 5;

    /**
     * How long should the status checker wait after it is started
     * before it starts its first run
     */
    private static final int INITIAL_CHECK_DELAY_SECONDS = 5;

    /**
     * The executor responsible for running the checker thread
     */
    private static final ScheduledExecutorService scheduler;

    static {
        statusStore = new ConcurrentHashMap<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                new StatusMonitor(),
                INITIAL_CHECK_DELAY_SECONDS,
                CHECK_PERIOD_IN_SECONDS,
                TimeUnit.SECONDS
        );
        logger.log(Level.INFO, "status service initialized");
    }

    /**
     * Stop the monitor thread
     */
    public static void stopService() {
        scheduler.shutdown();
    }

    /**
     * @param status incoming status update from
     */
    public void addStatusUpdate(Status status) {
        logger.log(Level.INFO, "storing incoming status update {0}", status);
        Status oldStatus = statusStore.put(status.getHost(), status);
        logger.log(Level.INFO, "status stored: {0}, old status: {1}", new Object[]{status, oldStatus});
    }

    /**
     * @param status
     * @return
     */
    private static boolean statusIsStale(Status status) {
        // last reported time stamp
        LocalDateTime dt = null;
        try {
            dt = LocalDateTime.parse(status.getTimeStamp(),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (java.time.format.DateTimeParseException e) {
            logger.log(Level.WARNING, "problems parsing incoming dates", e);
            return true; // blindly say its stale since we cannot parse
        }

        // now
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime mustHaveCheckedInBy = dt.plusSeconds(ALLOWED_QUIET_PERIOD_SECONDS);

        if(mustHaveCheckedInBy.isBefore(now)) {
            String latePeriod = calculateLatePeriod(mustHaveCheckedInBy, now);
            logger.log(Level.INFO, "stale status: {0}, is late by: {1}", new Object[]{status, latePeriod});
            return true;
        }

        return false;
    }

    /**
     * Returns a string representing the difference in time period between
     * two LocalDateTime objects
     *
     * @param expectedCheckInTime start time
     * @param now end time
     * @return difference between start and end
     */
    private static String calculateLatePeriod(LocalDateTime expectedCheckInTime, LocalDateTime now) {
        LocalDateTime late = now
                .minusDays(expectedCheckInTime.getDayOfYear())
                .minusHours(expectedCheckInTime.getHour())
                .minusMinutes(expectedCheckInTime.getMinute())
                .minusSeconds(expectedCheckInTime.getSecond())
                .minusNanos(expectedCheckInTime.getNano());

        Period period = Period.between(now.toLocalDate(), expectedCheckInTime.toLocalDate());

        long days = period.getDays();

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
        if(days > 0) {
            lateString.insert(0, days == 1 ? " day, " : " days, ");
            lateString.insert(0, days);
        }

        return lateString.toString();
    }

    /**
     * Thread responsible for the periodic checking of
     * status updates, and escalation if status updates
     * are stale
     */
    private static class StatusMonitor implements Runnable {
        @Override
        public void run() {
            logger.log(Level.INFO, "status checker thread run started");

            for(Status status : statusStore.values()) {
                if(statusIsStale(status)) {
                    logger.log(Level.INFO, "escalating stale status: {0}", status);
                    // TODO escalate
                } else {
                    logger.log(Level.INFO, "status OK: {0}", status);
                }
            }

        }
    }

}
