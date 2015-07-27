package io.enforcer.xwing;

import java.util.Set;

/**
 * Created by kavehg on 7/27/2015.
 */
public interface ProcessFinder {

    /**
     * Given a string return all process Ids that contain this string
     *
     * @param searchFilter string by which to filter processes
     * @return process Ids matching search filter
     */
    Set<Integer> getMatchingProcessIds(String searchFilter);
}
