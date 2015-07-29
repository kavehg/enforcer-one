package io.enforcer.xwing;

import sun.jvmstat.monitor.*;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Finds java processes using the jdk provided tools.jar which we
 * will bundle in the x-wing binary. So this should work even if
 * only a jre is available
 *
 * Created by kavehg on 7/29/2015.
 */
public class JavaProcessFinder implements ProcessFinder {

    /**
     * Class logger
     */
    private static final Logger logger = Logger.getLogger(JavaProcessFinder.class.getName());

    /**
     * Finds java process ids and names of main classes
     *
     * @param searchFilter string by which to filter processes
     * @return set of matching java processes
     */
    @Override
    public Set<MonitoredProcess> getMatchingProcesses(String searchFilter) {
        HashSet<MonitoredProcess> monitoredProcesses = new HashSet<>();

        try {
            MonitoredHost local = MonitoredHost.getMonitoredHost("localhost");
            Set<Integer> processIds = new HashSet<>(local.activeVms());

            for (Integer id : processIds) {
                MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + id));
                String processName = MonitoredVmUtil.mainClass(vm, true);
                monitoredProcesses.add(new MonitoredProcess(id, processName));
            }
        } catch (MonitorException | URISyntaxException e) {
            logger.log(Level.SEVERE, "Problems getting java process details", e);
            e.printStackTrace();
        }

        return monitoredProcesses;
    }
}
