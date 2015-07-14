package io.enforcer.deathstar.pojos;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StatusTest {

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

}