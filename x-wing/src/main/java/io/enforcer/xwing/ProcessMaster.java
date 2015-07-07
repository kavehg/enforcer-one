package io.enforcer.xwing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
     * This file should be on the classpath and contains
     * various configuration parameters
     */
    private static final String configFileName = "config.properties";

    /**
     * This set contains all processes that are being monitored
     * and is being periodically updated by the monitoring thread
     */
    private Set<MonitoredProcess> currentlyMonitoredProcesses;

    /**
     * Properties loaded from configuration file
     */
    private Properties properties;

    /**
     * The periodic monitoring task is run by this scheduler
     */
    private ScheduledExecutorService scheduler;

    /**
     * Using this constructor, the process master will not
     * initialize itself but will rather wait to be handed the
     * starting set of monitored processes via the overrideStartingState method
     *
     * This is useful for unit testing the process master and
     * is not intended to be used for actual operation
     *
     * @param getInitialProcessSnapshot should the process master initialize itself
     */
    public ProcessMaster(Boolean getInitialProcessSnapshot) {
        loadConfiguration();
        if(getInitialProcessSnapshot)
            initProcessList();
    }

    /**
     * Using this constructor, the process master will determine
     * the starting set of live processes by itself. This constructor
     * is meant to be used for actual operation
     */
    public ProcessMaster() {
        this(true);
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
     * @param initialProcessSnapshot initial snapshot of processes
     */
    public void overrideStartingState(Set<MonitoredProcess> initialProcessSnapshot) {
        currentlyMonitoredProcesses = initialProcessSnapshot;
    }

    /**
     * This method outputs the configuration as well as the current
     * list of monitored processes to the logs
     */
    public void logCurrentState() {
        logger.log(Level.INFO, dumpConfiguration());
        logger.log(Level.INFO, dumpCurrentState());
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
     * Loads properties from the config file
     */
    private void loadConfiguration() {
        properties = new Properties();
        InputStream inputStream = null;

        inputStream = ProcessMaster.class.getClassLoader().getResourceAsStream(configFileName);
        if(inputStream == null)
            logger.log(Level.SEVERE, "could not load config file: {0}", configFileName);

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "could not load properties from input stream", e);
        }
    }

    /**
     * @return String representation of the properties loaded from config file
     */
    private String dumpConfiguration() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for(String key : properties.stringPropertyNames()) {
            sb.append("\t");
            sb.append(key);
            sb.append(" : ");
            sb.append(properties.getProperty(key));
        }
        return sb.toString();
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
     * @param stringProcesses
     * @return
     */
    private Set<MonitoredProcess> convertStringsToProcess(List<String> stringProcesses) {
        return stringProcesses.stream()
                .map(this::convertJPSString)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * @param jpsString
     * @return
     */
    private MonitoredProcess convertJPSString(String jpsString) {
        StringTokenizer tokenizer = new StringTokenizer(jpsString, " ");
        Integer processId = 0;
        String mainClass = "UNKNOWN";
        if(tokenizer.hasMoreTokens())
            processId = Integer.parseInt(tokenizer.nextToken());
        else
            logger.log(Level.WARNING, "could not get process id for string: {0}", jpsString);

        if(tokenizer.hasMoreTokens())
            mainClass = tokenizer.nextToken();
        else
            logger.log(Level.WARNING, "could not get main class for string: {0}", jpsString);

        return new MonitoredProcess(processId, mainClass);
    }

    /**
     * @return
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
     * @param start
     * @param current
     * @return
     */
    public Set<MonitoredProcessDiff> compareProcessSets(Set<MonitoredProcess> start, Set<MonitoredProcess> current) {

        Set<MonitoredProcessDiff> additions = checkForAdditions(start, current);
        Set<MonitoredProcessDiff> removals = checkForRemovals(start, current);

        additions.addAll(removals);

        return additions; // actually additions + removals
    }

    /**
     * @param start
     * @param current
     * @return
     */
    private Set<MonitoredProcessDiff> checkForAdditions(Set<MonitoredProcess> start, Set<MonitoredProcess> current) {
        HashSet<MonitoredProcess> currentProcesses = new HashSet<>(current);
        currentProcesses.removeAll(start);
        return convertProcessesToDiffs(currentProcesses, ProcessStateChanges.ADDED);
    }

    /**
     * @param start
     * @param current
     * @return
     */
    private Set<MonitoredProcessDiff> checkForRemovals(Set<MonitoredProcess> start, Set<MonitoredProcess> current) {
        HashSet<MonitoredProcess> startingProcesses = new HashSet<>(start);
        startingProcesses.removeAll(current);
        return convertProcessesToDiffs(startingProcesses, ProcessStateChanges.REMOVED);
    }

    /**
     * @param processes
     * @param stateChange
     * @return
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
        }

    }
}
