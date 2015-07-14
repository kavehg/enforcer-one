package io.enforcer.xwing;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * XWing - a lightweight java process monitor
 */
public class XWing {

    private static final Logger logger = Logger.getLogger(XWing.class.getName());
    private static final ConsoleHandler consoleHandler = new ConsoleHandler();
    private static final XWingConfiguration config = new XWingConfiguration(null);

    private ProcessMaster processMaster;

    public static void main(String[] args) {
        // Remove default loggers
        Logger globalLogger = Logger.getLogger("");
        Handler[] handlers = globalLogger.getHandlers();
        for(Handler handler : handlers) {
            globalLogger.removeHandler(handler);
        }

        // configure console logger & set levels
        consoleHandler.setLevel(Level.ALL);
        globalLogger.addHandler(consoleHandler);
        globalLogger.setLevel(Level.ALL);

        // start
        XWing xWing = new XWing();
        xWing.run();
    }

    public XWing() {
        // start the process master and monitor
        processMaster = new ProcessMaster();
        processMaster.startProcessMonitoring();
    }

    private void run() {
        processMaster.logCurrentState();
    }

    public static XWingConfiguration getConfig() {
        return config;
    }
}
