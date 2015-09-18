package io.enforcer.xwing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kavehg on 7/27/2015.
 */
public class LinuxProcessFinder implements ProcessFinder {

    /**
     * Class logger
     */
    private static final Logger logger = Logger.getLogger(LinuxProcessFinder.class.getName());

    /**
     * Specialized finder used for java processes
     */
    private JavaProcessFinder javaProcessFinder;

    /**
     * Constructor
     */
    public LinuxProcessFinder() {
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
            MonitoredProcess monitoredProcess = convertPsString(line);
            if(monitoredProcess != null)
                extractedProcesses.add(monitoredProcess);
        }
        return extractedProcesses;
    }

    /**
     * Given one line of ps output this method attempts to parse
     * and convert to a MonitoredProcess object
     *
     * @param psOutput string representing one line of ps output
     * @return java object representing the given string or null if input cannot be parsed
     */
    private MonitoredProcess convertPsString(String psOutput) {
        StringTokenizer st = new StringTokenizer(psOutput, "|");
        MonitoredProcess process = null;

        if(st.countTokens() < 2) {
            logger.log(Level.WARNING, "Problems parsing this string returned by ps: {0}", psOutput);
            return null;
        }

        String pidString = st.nextToken();
        String command = st.nextToken();

        try {
            int pid = Integer.parseInt(pidString.trim());
            process = new MonitoredProcess(pid, command);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Unable to parse pid in this string: {0}", psOutput);
        }

        if(st.hasMoreTokens() && process != null) {
            process.setArguments(st.nextToken());
        }
        return process;
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
     * For Linux the parameters to ps are:
     *
     *  %P => process id
     *  %c => command
     *  %a => arguments to the command
     *
     * Thus the output will contain three columns separated by the pipe
     * character '|'
     *
     * @param searchFilter process to look for
     * @return command to execute
     */
    private String buildSearchCommand(String searchFilter) {
        StringBuilder sb = new StringBuilder();
        sb.append("ps -eo \"%P|%c|%a\" | grep  ");
        sb.append(searchFilter);
        return sb.toString();
    }
}
