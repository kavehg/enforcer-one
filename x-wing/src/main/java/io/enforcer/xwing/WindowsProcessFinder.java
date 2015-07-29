package io.enforcer.xwing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kavehg on 7/27/2015.
 *
 * The windows process finder runs the tasklist command to find
 * all processes that match a filter and converts the output
 * of that command to a collection of MonitoredProcess objects
 */
public class WindowsProcessFinder implements ProcessFinder {

    /**
     * Class logger
     */
    private static final Logger logger = Logger.getLogger(WindowsProcessFinder.class.getName());

    /**
     * Specialized finder used for java processes
     */
    private JavaProcessFinder javaProcessFinder;

    /**
     * Constructor
     */
    public WindowsProcessFinder() {
        javaProcessFinder = new JavaProcessFinder();
    }

    /**
     * Returns the process identifiers that match the provided search string.
     * This is achieved by executing the search command and subsequently
     * extracting process details from each returned line.
     *
     * If the provided search string (which comes from the 'included' property
     * in the config) is "java" then the JavaProcessFinder is used.
     *
     * @param searchFilter string by which to filter processes
     * @return matching process identifiers
     */
    @Override
    public Set<MonitoredProcess> getMatchingProcesses(String searchFilter) {
        if(searchFilter.equalsIgnoreCase("java"))
            return javaProcessFinder.getMatchingProcesses(searchFilter);

        Set<String> commandOutput = executeSearchCommand(searchFilter);
        return extractProcessDetails(commandOutput);
    }

    /**
     * Given the output of the search command, extract the process details
     * of the matching processes
     *
     * @param commandOutput the result of the executed search command
     * @return
     */
    private Set<MonitoredProcess> extractProcessDetails(Set<String> commandOutput) {
        HashSet<MonitoredProcess> extractedProcesses = new HashSet<>();
        for(String line : commandOutput) {
            MonitoredProcess monitoredProcess = convertTaskListString(line);
            if(monitoredProcess != null)
                extractedProcesses.add(monitoredProcess);
        }
        return extractedProcesses;
    }

    /**
     * Given one line of tasklist output this method attempts to parse
     * and convert to a MonitoredProcess object
     *
     * @param taskListOutput string representing one line of tasklist output
     * @return java object representing the given string or null if input cannot be parsed
     */
    private MonitoredProcess convertTaskListString(String taskListOutput) {
        StringTokenizer st = new StringTokenizer(taskListOutput, ",");
        String processName = null;
        String processIdString = null;
        Integer processId = null;

        if(st.hasMoreTokens()) {
            processName = st.nextToken();
            if(st.hasMoreTokens()) {
                processIdString = st.nextToken();
                // use substring to drop the starting and ending double quotes from the tasklist output
                processId = Integer.parseInt(
                        processIdString.substring(1, processIdString.length() - 1)
                );
                return new MonitoredProcess(processId, processName.substring(1, processName.length() - 1));
            } else {
                logger.log(Level.WARNING, "Unable to extract process id from tasklist string", taskListOutput);
            }
        } else {
            logger.log(Level.WARNING, "Unable to extract process name from tasklist string", taskListOutput);
        }
        return null;
    }

    /**
     * Given a search filter return the output of the search command
     *
     * @param searchFilter string by which to filter processes
     * @return output of the search command
     */
    private Set<String> executeSearchCommand(String searchFilter) {
        HashSet<String> returnedStrings = new HashSet<>();
        try {
            Process p = Runtime.getRuntime().exec(buildSearchCommand(searchFilter));
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = input.readLine()) != null) {
                returnedStrings.add(line);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to get process identifiers", e);
        }
        return returnedStrings;
    }

    /**
     * Given a search string build the OS specific search command
     *
     * @param searchFilter process to look for
     * @return command to execute
     */
    private String buildSearchCommand(String searchFilter) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getenv("windir"));
        sb.append(File.separator);
        sb.append("system32");
        sb.append(File.separator);
        sb.append("cmd /C tasklist /fo csv /nh | find \"");
        sb.append(searchFilter);
        sb.append("\"");
        return sb.toString();
    }
}
