package io.enforcer.deathstar.pojos;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StatusTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testEquals() throws Exception {
        Status first = new Status(1, "host", "2015-02-25T12:27:00");
        Status second = new Status(1, "host", "2015-02-25T12:27:00");

        assertEquals(first, second);
    }

    @Test
    public void testEqualsRegardlessOfTimeStamp() throws Exception {
        Status first = new Status(1, "host", "9999-02-25T12:27:00");
        Status second = new Status(1, "host", "2015-02-25T12:27:00");

        assertEquals(first, second);
    }

    @Test
    public void testNotEqualsPIDDiff() throws Exception {
        Status first = new Status(1, "host", "2015-02-25T12:27:00");
        Status second = new Status(2, "host", "2015-02-25T12:27:00");

        assertNotEquals(first, second);
    }

    @Test
    public void testNotEqualsHostDiff() throws Exception {
        Status first = new Status(1, "host", "2015-02-25T12:27:00");
        Status second = new Status(1, "XYZ", "2015-02-25T12:27:00");

        assertNotEquals(first, second);
    }

    @Test
    public void testMapBehaviour() {
        // one entry
        Status first = new Status(1, "host", "2015-02-25T12:27:00");
        Set<Status> set = new HashSet<>();

        set.add(first);

        assertEquals(1, set.size());

        // overwrite entry
        Status second = new Status(1, "host", "2015-02-25T12:27:59");
        set.remove(second);
        set.add(second);

        assertEquals(1, set.size());

        for(Status status : set) {
            // should only be one
            assertEquals("2015-02-25T12:27:59", status.getTimeStamp());
        }
    }

    @Test
    public void parseStatusDates() {
        Status status = new Status(1, "host", "2015-01-02T6:03:03");

        LocalDateTime dt = LocalDateTime.parse(status.getTimeStamp(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        System.out.println(dt);
    }
}