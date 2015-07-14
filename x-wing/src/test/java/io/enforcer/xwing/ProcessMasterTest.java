package io.enforcer.xwing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ProcessMasterTest {

    private Set<Integer> startSet;
    private Set<MonitoredProcess> start;
    private Properties properties;
    private ProcessMaster processMaster;
    private XWingConfiguration config;

    @Before
    public void setUp() throws Exception {
        startSet = new HashSet<>();
        startSet.add(1);
        startSet.add(2);
        startSet.add(3);

        start = new HashSet<>();
        start.add(new MonitoredProcess(1, "Main One"));
        start.add(new MonitoredProcess(2, "Main Two"));
        start.add(new MonitoredProcess(3, "Main Three"));

        properties = new Properties();
        properties.setProperty("ignored", "IgnoreThisMainClass,IgnoreThatMainClass");

        config = new XWingConfiguration(properties);

        processMaster = new ProcessMaster(false, config);
        processMaster.setInitialProcessSnapshot(start);
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Reminder of how removeAll works and
     * what "asymmetric set difference" means
     */
    @Test
    public void testRemoval() {
        HashSet<Integer> endSet = new HashSet<>();
        endSet.add(1);
        endSet.add(2);

        // start.removeAll(end) -> deleted elements
        startSet.removeAll(endSet);

        HashSet<Integer> expectedResultSet = new HashSet<>();
        expectedResultSet.add(3);

        assertEquals(expectedResultSet, startSet);
    }

    /**
     * Reminder of how removeAll works and
     * what "asymmetric set difference" means
     */
    @Test
    public void testAddition() {
        HashSet<Integer> endSet = new HashSet<>();
        endSet.add(1);
        endSet.add(2);
        endSet.add(3);
        endSet.add(4);

        // end.removeAll(start) -> added elements
        endSet.removeAll(startSet);

        HashSet<Integer> expectedResultSet = new HashSet<>();
        expectedResultSet.add(4);

        assertEquals(expectedResultSet, endSet);
    }

    /**
     * Make sure that a removed process shows up in diffs
     */
    @Test
    public void testProcessRemovals() {
        HashSet<MonitoredProcess> end = new HashSet<>();
        end.add(new MonitoredProcess(1, "Main One"));
        end.add(new MonitoredProcess(2, "Main Two"));

        Set<MonitoredProcessDiff> processDiffs = processMaster.compareProcessSets(start, end);

        HashSet<MonitoredProcessDiff> expectedDiffs = new HashSet<>();
        expectedDiffs.add(new MonitoredProcessDiff(3, "Main Three", ProcessStateChanges.REMOVED));

        assertEquals(expectedDiffs, processDiffs);
    }

    /**
     * Make sure that an added process shows up in diffs
     */
    @Test
    public void testProcessAddition() {
        HashSet<MonitoredProcess> end = new HashSet<>();
        end.add(new MonitoredProcess(1, "Main One"));
        end.add(new MonitoredProcess(2, "Main Two"));
        end.add(new MonitoredProcess(3, "Main Three"));
        end.add(new MonitoredProcess(4, "Main Four"));

        Set<MonitoredProcessDiff> processDiffs = processMaster.compareProcessSets(start, end);

        HashSet<MonitoredProcessDiff> expectedDiffs = new HashSet<>();
        expectedDiffs.add(new MonitoredProcessDiff(4, "Main Four", ProcessStateChanges.ADDED));

        assertEquals(expectedDiffs, processDiffs);
    }

    /**
     * Make sure that an ignored process does NOT show in diffs
     */
    @Test
    public void testIgnoredProcesses() {
        HashSet<MonitoredProcess> end = new HashSet<>();
        end.add(new MonitoredProcess(1, "IgnoreThisMainClass"));
        end.add(new MonitoredProcess(2, "IgnoreThatMainClass"));
        end.addAll(start);

        Set<MonitoredProcessDiff> processDiffs = processMaster.compareProcessSets(start, end);

        HashSet<MonitoredProcessDiff> expectedDiffs = new HashSet<>();

        assertEquals(expectedDiffs, processDiffs);
    }
}