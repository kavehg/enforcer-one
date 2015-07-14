package io.enforcer.xwing;

import javax.management.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by kaveh on 2/23/2015.
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
    private Set<MonitoredProcess> currentlyMonitoredProcesses;

    /**
     * These processes will be ignored by the process monitor.
     * They can be defined in the property file by setting the
     * 'ignored' property equal to a comma separated list of
     * main classes that should be ignored
     */
    private Set<String> ignoredProcesses;

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
     * Using this constructor, the process master will not
     * initialize itself but will rather wait to be handed the
     * starting set of monitored processes via the setInitialProcessSnapshot
     * method. Similarly, it is possible to override the properties
     * instead of reading them from the configuration file.
     *
     * This is useful for unit testing the process master and
     * is not intended to be used for actual operation
     *
     * @param getInitialProcessSnapshot should the process master initialize itself
     */
    public ProcessMaster(Boolean getInitialProcessSnapshot, XWingConfiguration configOverride) {
        problematicProcessIds = new HashSet<>();

        if(configOverride != null)
            this.config = configOverride;
        else
            this.config = XWing.getConfig();

        initIgnoredProcesses();

        if(getInitialProcessSnapshot)
            initProcessList();

        // register mbean for jmx monitoring
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("io.enforcer.xwing:type=ProcessMaster");
            mbs.registerMBean(this, name);
        } catch (   MalformedObjectNameException | NotCompliantMBeanException |
                InstanceAlreadyExistsException | MBeanRegistrationException e) {
            logger.log(Level.SEVERE, "could not register mbean", e);
        }
    }

    /**
     * Using this constructor, the process master will determine
     * the starting set of live processes by itself. This constructor
     * is meant to be used for actual operation
     */
    public ProcessMaster() {
        this(true, null);
    }

    /**
     * Allows clients of this class to determine when to start the
     * process monitor. This is useful for unit testing.
     */
    public void startProcessMonitoring() {
        startScheduler();
    }

    /**
     * Allows a client to reset the starting state and provide
     * the ProcessMaster an initial list of processes. This is
     * useful for unit testing of the process master.
     *
     * @param processSnapshot initial snapshot of processes
     */
    public void setInitialProcessSnapshot(Set<MonitoredProcess> processSnapshot) {
        currentlyMonitoredProcesses = processSnapshot;
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
    private void initIgnoredProcesses() {
        ignoredProcesses = new HashSet<>();
        String ignored = config.getProperty("ignored");
        if(ignored != null) {
            StringTokenizer st = new StringTokenizer(ignored, ",");
            while(st.hasMoreTokens()) {
                ignoredProcesses.add(st.nextToken());
            }
            logger.log(Level.INFO, "Ignoring the following processes: " + ignoredProcesses);
        }
    }

    /**
     * @return String representation of the monitored processes
     */
    private String dumpCurrentState() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for(MonitoredProcess process : currentlyMonitoredProcesses) {
            sb.append("\t");
            sb.append(process.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Gets a snapshot of all running processes and initializes the
     * currentlyMonitoredProcesses set
     */
    private void initProcessList() {
        currentlyMonitoredProcesses = new HashSet<>();
        currentlyMonitoredProcesses.addAll(getProcessSnapshot());
    }

    /**
     * @return a snapshot of monitored processes
     */
    private Set<MonitoredProcess> getProcessSnapshot() {
        HashSet<MonitoredProcess> monitoredProcesses = new HashSet<>();
        List<String> processStrings = getListOfProcessStrings();
        monitoredProcesses.addAll(convertStringsToProcess(processStrings));
        return monitoredProcesses;
    }

    /**
     * Takes a list of strings which are returned by the jps command
     * and turns them into a set of MonitoredProcess objects using the
     * convertJPSString() method
     *
     * @param stringProcesses list of strings returned by jps
     * @return collection of MonitoredProcess objects representing the passed in strings
     */
    private Set<MonitoredProcess> convertStringsToProcess(List<String> stringProcesses) {
        return stringProcesses.stream()
                .map(this::convertJPSString)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Takes the output of the jps command and turns it into
     * a MonitoredProcess object
     *
     * @param jpsString one line of jps output
     * @return an instance of the MonitoredProcess class
     */
    private MonitoredProcess convertJPSString(String jpsString) {
        StringTokenizer tokenizer = new StringTokenizer(jpsString, " ");
        Integer processId = 0;
        String mainClass = "UNKNOWN";

        // determine the pid
        if(tokenizer.hasMoreTokens())
            processId = Integer.parseInt(tokenizer.nextToken());
        else
            logger.log(Level.WARNING, "could not get process id for string: {0}", jpsString);

        // determine the main class
        if(tokenizer.hasMoreTokens()) {
            mainClass = tokenizer.nextToken();
        } else if(!problematicProcessIds.contains(processId)) {
            problematicProcessIds.add(processId);
            logger.log(Level.WARNING, "could not get main class for string: {0}", jpsString);
        }

        return new MonitoredProcess(processId, mainClass);
    }

    /**
     * Run the jps command and determine which java processes are
     * currently running.
     *
     * @return list of strings returned by jps
     */
    private List<String> getListOfProcessStrings() {
        ArrayList<String> listOfProcessStrings = new ArrayList<>();
        try {
            String line;
            Process p = Runtime.getRuntime().exec("jps");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                listOfProcessStrings.add(line);
            }
            input.close();
        } catch (IOException ioException) {
            logger.log(Level.SEVERE, ioException.toString(), ioException);
        }
        return listOfProcessStrings;
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
     * @param processesToBeFiltered
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
        private Set<MonitoredProcess> startingSnapshot = currentlyMonitoredProcesses;

        /**
         * get an updated snapshot of running processes and compare to
         * our starting state
         */
        @Override
        public void run() {
            currentlyMonitoredProcesses = getProcessSnapshot();
            Set<MonitoredProcessDiff> monitoredProcessDiffs = compareProcessSets(startingSnapshot, currentlyMonitoredProcesses);
            // TODO escalate diffs
            logger.log(Level.FINE, "Identified differences: " + monitoredProcessDiffs);
            startingSnapshot = currentlyMonitoredProcesses;
        }

    }
}
