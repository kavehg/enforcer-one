package io.enforcer.xwing;

import java.util.Set;

/**
 * Created by kaveh on 2/24/2015.
 */
public interface ProcessMasterMBean {
    void logCurrentState();
    void stopScheduler();
    Set<Integer> getProblematicProcessIds();
    Set<String> getIgnoredProcesses();
}
