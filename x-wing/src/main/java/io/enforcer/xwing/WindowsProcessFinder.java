package io.enforcer.xwing;

import java.util.Set;

/**
 * Created by kavehg on 7/27/2015.
 */
public class WindowsProcessFinder implements ProcessFinder {

    @Override
    public Set<Integer> getMatchingProcessIds(String searchFilter) {

        // run tasklist
        // Process p = Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe");

        return null;
    }
}
