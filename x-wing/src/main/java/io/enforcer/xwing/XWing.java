package io.enforcer.xwing;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * XWing - a lightweight java process monitor
 */
public class XWing {

    private static final Logger logger = Logger.getLogger(XWing.class.getName());

    private ProcessMaster processMaster;

    public static void main(String[] args) {
        XWing xWing = new XWing();
        xWing.run();
    }

    public XWing() {
        // start the process master and monitor
        processMaster = new ProcessMaster();
        processMaster.startProcessMonitoring();

        // register mbean for jmx monitoring
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("io.enforcer.xwing:type=ProcessMaster");
            mbs.registerMBean(processMaster, name);
        } catch (   MalformedObjectNameException | NotCompliantMBeanException |
                    InstanceAlreadyExistsException | MBeanRegistrationException e) {
            logger.log(Level.SEVERE, "could not register mbean", e);
        }
    }

    private void run() {
        processMaster.logCurrentState();
    }

}
