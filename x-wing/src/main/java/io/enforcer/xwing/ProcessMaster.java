package io.enforcer.xwing;

import io.enforcer.deathstar.DeathStarClient;
import org.apache.commons.lang3.SystemUtils;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by kavehg on 2/23/2015.
 */
public class ProcessMaster implements ProcessMasterMBean {

    /**
     * Logger to be used in class
     */
    private static final Logger logger = Logger.getLogger(ProcessMaster.class.getName());

    /**
     * Configuration properties
     */
    private XWingConfiguration config;

    /**
     * This set contains all processes that are being monitored
     * and is being periodically updated by the monitoring thread
     */
    private Set<MonitoredProcess> currentProcessSnapshot;

    /**
     * These processes will be ignored by the process monitor.
     * They can be defined in the property file by setting the
     * 'ignored' property equal to a comma separated list of
     * main classes that should be ignored
     */
    private Set<String> ignoredProcesses;

    /**
     * These processes will be included and searched for by the
     * process monitor. They can be defined in the property file
     * by setting the 'included' property equal to a comma
     * separated list of strings that identify the processes that
     * we are interested in
     */
    private Set<String> includedProcesses;

    /**
     * This set contains any process IDs that we are having problems
     * with. This includes processes that have no main class
     * according to jps. Processes in this set get logged and
     * ignored going forward.
     */
    private Set<Integer> problematicProcessIds;

    /**
     * The periodic monitoring task is run by this scheduler
     */
    private ScheduledExecutorService scheduler;

    /**
     * Our connection to the death star
     */
    private DeathStarClient deathStar;

    /**
     * Using this constructor, the process master will determine
     * the starting set of live processes by itself. This constructor
     * is meant to be used for production operation
     */
    public ProcessMaster() {
        this(true, null, true, true);
    }

    /**
     * Using this constructor, the process master can skip initializing itself
     * and rather wait to be handed the starting set of monitored processes via
     * the setInitialProcessSnapshot method. Similarly, it is possible to override
     * the properties instead of reading them from the configuration file.
     *
     * This is useful for unit testing the process master and is not intended to
     * be used for actual operation
     *
     * @param getInitialProcessSnapshot should the process master initialize itself
     */
    public ProcessMaster(Boolean getInitialProcessSnapshot,
                         XWingConfiguration configOverride,
                         Boolean registerJMXMBean,
                         Boolean connectToDeathStar) {
        problematicProcessIds = new HashSet<>();

        if(configOverride != null)
            this.config = configOverride;
        else
            this.config = XWing.getConfig();

        if(connectToDeathStar)
            deathStar = connectToDeathStar();

        ignoredProcesses = initIgnoredProcesses();

        if(getInitialProcessSnapshot)
            initProcessList();

        if(registerJMXMBean) {
            // register mbean for jmx monitoring
            try {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                ObjectName name = new ObjectName("io.enforcer.xwing:type=ProcessMaster");
                mbs.registerMBean(this, name);
            } catch (MalformedObjectNameException | NotCompliantMBeanException |
                    InstanceAlreadyExistsException | MBeanRegistrationException e) {
                logger.log(Level.SEVERE, "could not register mbean", e);
            }
        }
    }

    /**
     * Allows clients of this class to determine when to start the
     * process monitor. This is useful for unit testing.
     */
    public void startProcessMonitoring() {
        startScheduler();
    }

    /**
     * Retrieves death star connection details from configuration and
     * returns an instance of the DeathStarClient
     *
     * @return death star connection
     */
    private DeathStarClient connectToDeathStar() {
        String deathStarHost = config.getProperty("deathStarHost");
        String deathStarPort = config.getProperty("deathStarPort");
        Integer deathStarPortInt = null;

        if(deathStarHost == null) {
            logger.log(Level.SEVERE, "Could not find deathStarHost in the configuration");
            return null;
        }

        if(deathStarPort == null) {
            logger.log(Level.SEVERE, "Could not find deathStarPort in the configuration");
            return null;
        } else {
            deathStarPortInt = Integer.parseInt(deathStarPort);
            return new DeathStarClient(deathStarHost, deathStarPortInt);
        }
    }

    /**
     * Allows a client to reset the starting state and provide
     * the ProcessMaster an initial list of processes. This is
     * useful for unit testing of the process master.
     *
     * @param processSnapshot initial snapshot of processes
     */
    public void setInitialProcessSnapshot(Set<MonitoredProcess> processSnapshot) {
        currentProcessSnapshot = processSnapshot;
    }

    /**
     * This method outputs the list of monitored processes to the logs
     */
    public void logCurrentState() {
        logger.log(Level.INFO, dumpCurrentState());
    }

    /**
     * @return current list of processes that are ignored by the monitor
     */
    public Set<String> getIgnoredProcesses() {
        return ignoredProcesses;
    }

    /**
     * @return current list of problematic process IDs
     */
    public Set<Integer> getProblematicProcessIds() {
        return problematicProcessIds;
    }

    /**
     * Stops the monitoring process
     */
    public void stopScheduler() {
        logger.log(Level.INFO, "stopping the process monitor");
        scheduler.shutdownNow();
    }

    /**
     * Starts the monitoring process
     */
    private void startScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new ProcessMonitor(), 5, 5, TimeUnit.SECONDS);
    }

    /**
     * Checks for the 'ignored' property in the config file and
     * adds all the processes that are to be ignored to a set
     */
    private Set<String> initIgnoredProcesses() {
        Set<String> ignoredProcesses = new HashSet<>();
        String ignored = config.getProperty("ignored");
        if(ignored != null) {
            StringTokenizer st = new StringTokenizer(ignored, ",");
            while(st.hasMoreTokens()) {
                ignoredProcesses.add(st.nextToken());
            }
            logger.log(Level.INFO, "Ignoring the following processes: " + ignoredProcesses);
        }
        return ignoredProcesses;
    }

    /**
     * @return String representation of the monitored processes
     */
    private String dumpCurrentState() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for(MonitoredProcess process : currentProcessSnapshot) {
            sb.append("\t");
            sb.append(process.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Gets a snapshot of all running processes and initializes the
     * currentProcessSnapshot set
     */
    private void initProcessList() {
        currentProcessSnapshot = new HashSet<>();
        currentProcessSnapshot.addAll(getProcessSnapshot());
    }

    /**
     * Checks the OS and identifies processes that are to be monitored
     * using the included property from the config file
     *
     * @return a snapshot of monitored processes
     */
    private Set<MonitoredProcess> getProcessSnapshot() {
        if(SystemUtils.IS_OS_WINDOWS)
            return getProcessSnapshotOnWindows();

        return getProcessSnapshotOnLinux();
    }

    /**
     * Gets a list of included process names from the configuration and
     * retrieves the matching processes for each string using the tasklist
     * command
     *
     * @return a collection of all matching processes
     */
    private Set<MonitoredProcess> getProcessSnapshotOnWindows() {
        HashSet<MonitoredProcess> matchingProcesses = new HashSet<>();
        WindowsProcessFinder finder = new WindowsProcessFinder();

        for(String processFilter : getIncludedProcesses()) {
            matchingProcesses.addAll(finder.getMatchingProcesses(processFilter));
        }

        return matchingProcesses;
    }

    /**
     * Gets a list of included process names from the configuration and
     * retrieves the matching processes for each string using the tasklist
     * command
     *
     * @return a collection of all matching processes
     */
    private Set<MonitoredProcess> getProcessSnapshotOnLinux() {
        Set<MonitoredProcess> matchingProcesses = new HashSet<>();
        LinuxProcessFinder processFinder = new LinuxProcessFinder();

        for(String processFilter : getIncludedProcesses()) {
            matchingProcesses.addAll(processFinder.getMatchingProcesses(processFilter));
        }

        return matchingProcesses;
    }

    /**
     * Create a set of strings from the 'included' property in the config
     * file and store in includedProcesses. If already created, just return
     * the set
     *
     * @return set of strings representing processes that are to be included
     */
    public Set<String> getIncludedProcesses() {
        // return the set if already created
        if (this.includedProcesses != null)
            return this.includedProcesses;

        // if first time, create the set and populate from config
        this.includedProcesses = new HashSet<>();
        String included = config.getProperty("included");

        if (included == null || included.isEmpty()) {
            logger.log(Level.INFO, "No processes appear to be included for monitoring in the " +
                    "config file. Use property 'included'");
            return includedProcesses;
        }

        StringTokenizer st = new StringTokenizer(included, ",");
        if (!st.hasMoreTokens()) {
            logger.log(Level.FINE, "No processes appear to be included for monitoring in the " +
                    "config file. Use property 'included' and separate entries by commas");
            return includedProcesses;
        }

        while (st.hasMoreTokens())
            includedProcesses.add(st.nextToken());

        return includedProcesses;
    }

    /**
     * Compares two sets of processes and determines the difference
     * between the starting set and the current set.
     * The differences are returned in the form of a set of process
     * differences.
     *
     * @param start     set of monitored processes that we started with
     * @param current   set of monitored processes that we ended with
     * @return differences between the two input sets
     */
    public Set<MonitoredProcessDiff> compareProcessSets(Set<MonitoredProcess> start, Set<MonitoredProcess> current) {

        Set<MonitoredProcessDiff> additions = checkForAdditions(start, current);
        Set<MonitoredProcessDiff> removals = checkForRemovals(start, current);

        // combine additions and removals
        additions.addAll(removals);

        // filter main classes that are to be ignored
        filterProcesses(additions);

        return additions; // actually additions + removals
    }

    /**
     * Given a list of processes, check if any are to be ignored
     * according to the 'ignored' property in the config file.
     * If yes, remove from set.
     *
     * @param processesToBeFiltered processes that will be checked
     */
    private void filterProcesses(Set<MonitoredProcessDiff> processesToBeFiltered) {
        Set<MonitoredProcessDiff> processDiffsToIgnore = processesToBeFiltered.stream().filter(
                diff -> ignoredProcesses.contains(diff.getMainClass())
        ).collect(Collectors.toSet());

        processesToBeFiltered.removeAll(processDiffsToIgnore);
    }

    /**
     * Use simple set operations to compare currently running processes
     * to what was running previously and determine if any processes
     * were added.
     *
     * @param start     starting set of processes
     * @param current   currently running processes
     * @return  set of added processes
     */
    private Set<MonitoredProcessDiff> checkForAdditions(Set<MonitoredProcess> start, Set<MonitoredProcess> current) {
        HashSet<MonitoredProcess> currentProcesses = new HashSet<>(current);
        currentProcesses.removeAll(start);
        return convertProcessesToDiffs(currentProcesses, ProcessStateChanges.ADDED);
    }

    /**
     * Use simple set operations to compare currently running processes
     * to what was running previously and determine if any processes
     * were terminated
     *
     * @param start     starting set of processes
     * @param current   currently running processes
     * @return  set of removed processes
     */
    private Set<MonitoredProcessDiff> checkForRemovals(Set<MonitoredProcess> start, Set<MonitoredProcess> current) {
        HashSet<MonitoredProcess> startingProcesses = new HashSet<>(start);
        startingProcesses.removeAll(current);
        return convertProcessesToDiffs(startingProcesses, ProcessStateChanges.REMOVED);
    }

    /**
     * Create diff objects representing changes in process states
     *
     * @param processes     processes for which diffs need to be created
     * @param stateChange   how the processes have changed
     * @return  objects representing process differences
     */
    private Set<MonitoredProcessDiff> convertProcessesToDiffs(Set<MonitoredProcess> processes, ProcessStateChanges stateChange) {
        HashSet<MonitoredProcessDiff> processDiffs = new HashSet<>();
        for(MonitoredProcess process : processes) {
            MonitoredProcessDiff processDiff = new MonitoredProcessDiff(process.getProcessId(), process.getMainClass(), stateChange);
            processDiffs.add(processDiff);
        }
        return processDiffs;
    }

    /**
     * The process monitor is the scheduled job that is responsible for
     * re-checking the current state of processes
     */
    private class ProcessMonitor implements Runnable {

        /**
         * let's remember the state prior to starting our monitoring run
         */
        private Set<MonitoredProcess> startingSnapshot = currentProcessSnapshot;

        /**
         * get an updated snapshot of running processes and compare to
         * our starting state
         */
        @Override
        public void run() {
            currentProcessSnapshot = getProcessSnapshot();
            Set<MonitoredProcessDiff> processDiffs = compareProcessSets(startingSnapshot, currentProcessSnapshot);
            // TODO escalate diffs
            logger.log(Level.FINE, "Identified differences: " + processDiffs);
            startingSnapshot = currentProcessSnapshot;
        }

    }
}
