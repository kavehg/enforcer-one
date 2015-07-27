package io.enforcer.xwing;

import java.util.*;
import java.util.concurrent.TimeUnit;

import sun.jvmstat.monitor.*;

public class ListJavaProcess {
    public static void main(String[] args) throws Exception {

        long start = System.nanoTime();
        /* Checking for local Host, one can do for remote machine as well */
        MonitoredHost local = MonitoredHost.getMonitoredHost("localhost");
        /* Take all active VM's on Host, LocalHost here */
        Set vmlist = new HashSet(local.activeVms());
        for (Object id : vmlist) {
            /* 1234 - Specifies the Java Virtual Machine identified by lvmid 1234 on an unnamed host.
            This string is transformed into the absolute form //1234, which must be resolved against
            a HostIdentifier. */
            MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + id));
            /* take care of class file and jar file both */
            String processname = MonitoredVmUtil.mainClass(vm, true);
            System.out.println(id + " ------> " + processname);
        }
        long elapsed = System.nanoTime() - start;
        System.out.println("Elapsed: " + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS) + " ms");
        // takes ~ 80 ms
    }
}