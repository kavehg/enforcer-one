package io.enforcer.xwing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kavehg on 7/26/15.
 */
public class ShellUtils {

    private static final Logger logger = Logger.getLogger(ShellUtils.class.getName());

    /**
     * Execute given command and returned the output as a list
     * of strings (one string per line)
     *
     * @param command to execute on shell
     * @return list of strings returned by the output of the provided command
     */
    private static List<String> executeCommand(String command) {
        List<String> listOfReturnedStrings = new ArrayList<>();
        try {
            String line;
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                listOfReturnedStrings.add(line);
            }
            input.close();
        } catch (IOException ioException) {
            logger.log(Level.SEVERE, ioException.toString(), ioException);
        }

        return listOfReturnedStrings;
    }
}
