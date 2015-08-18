package io.enforcer.deathstar.services;

import io.enforcer.deathstar.pojos.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by kavehg on 7/14/2015.
 */
public class StatusServiceTest {

    private StatusService statusService;
    private static final Integer initialCheckDelayInSeconds = 1;
    private static final Integer checkPeriodInSeconds = 1;

    @Before
    public void setUp() throws Exception {
        statusService = new StatusService(initialCheckDelayInSeconds, checkPeriodInSeconds);
    }

    @After
    public void tearDown() throws Exception {
        statusService.stopService();
    }

    @Test
    public void testAddStatusUpdate() throws Exception {
        Status status = new Status(1, "host1", "2012-01-01T00:00:00Z");
        statusService.addStatusUpdate(status);

        assertEquals((Integer) 1, statusService.getNumberOfStatusUpdatesReceived());
    }

    @Test
    public void testGetLatestStatusByHost() throws Exception {
        String host = "host1";
        Status status = new Status(1, host, "2012-01-01T00:00:00Z");
        statusService.addStatusUpdate(status);

        assertEquals(status, statusService.getLatestStatusByHost(host));
    }

//    @Test
    public void testDetectDelinquentHost() throws Exception {
        // time stamp is in the past
        Status status = new Status(1, "host1", "2012-01-01T00:00:00Z");
        statusService.addStatusUpdate(status);
        statusService.startStatusMonitoring();

        // give the status checker a chance to run
        Thread.sleep(checkPeriodInSeconds * 2 * 1000);

        assertTrue(statusService.isHostDelinquent("host1"));
    }

//    @Test
    public void testGetNumberOfDelinquentHosts() throws Exception {
        // time stamp is in the past
        Status status = new Status(1, "host1", "2012-01-01T00:00:00Z");
        statusService.addStatusUpdate(status);
        statusService.startStatusMonitoring();

        // give the status checker a chance to run
        Thread.sleep(checkPeriodInSeconds * 2 * 1000);

        assertEquals((Integer) 1, statusService.getNumberOfDelinquentHosts());
    }

//    @Test
    public void testGetDelinquentHostLastUpdate() throws Exception {
        // time stamp is in the past
        String host = "host1";
        String timeStamp = "2012-01-01T00:00:00Z";
        Status status = new Status(1, host, timeStamp);
        statusService.addStatusUpdate(status);
        statusService.startStatusMonitoring();

        // give the status checker a chance to run
        Thread.sleep(checkPeriodInSeconds * 2 * 1000);

        assertEquals((Integer) 1, statusService.getNumberOfDelinquentHosts());
        assertEquals(Instant.parse(timeStamp), statusService.getDelinquentHostLastUpdate(host));
    }
}