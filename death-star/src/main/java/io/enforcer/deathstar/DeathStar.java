package io.enforcer.deathstar;

import io.enforcer.deathstar.services.StatusService;
import io.enforcer.deathstar.ws.WebSocketServer;
import javassist.bytecode.analysis.Executor;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DeathStar class
 */
public class DeathStar {

    private static final Logger logger = Logger.getLogger(DeathStar.class.getName());
    private static final ConsoleHandler consoleHandler = new ConsoleHandler();

    // Base URI the Grizzly HTTP server will listen on
    public static final String API_BASE_URI = "http://localhost:8000/api/";

    private static StatusService statusService;

    private static WebSocketServer webSocketServer;

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startHttpServer() throws IOException {
        // create a resource config that scans for JAX-RS resources and providers
        // in the specified package
        final ResourceConfig rc = new ResourceConfig().packages("io.enforcer.deathstar.rest");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at API_BASE_URI
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(API_BASE_URI), rc);

        // serve static content from the resources/client directory
        HttpHandler httpHandler = new CLStaticHttpHandler(HttpServer.class.getClassLoader(), "client/");
        httpServer.getServerConfiguration().addHttpHandler(httpHandler, "/");

        return httpServer;
    }

    /**
     * DeathStar method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // Remove default loggers
        Logger globalLogger = Logger.getLogger("");
        Handler[] handlers = globalLogger.getHandlers();
        for(Handler handler : handlers) {
            globalLogger.removeHandler(handler);
        }

        // configure console logger & set levels
        consoleHandler.setLevel(Level.INFO);
        globalLogger.addHandler(consoleHandler);
        globalLogger.setLevel(Level.ALL);

        statusService = new StatusService(5, 5);
        statusService.startStatusMonitoring();

        final HttpServer server = startHttpServer();
        webSocketServer = new WebSocketServer(9090);

        ExecutorService webSocketServerThread = Executors.newSingleThreadExecutor();
        webSocketServerThread.submit(webSocketServer);

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        server.stop();
        statusService.stopService();
    }

    /**
     * Obtain reference to status service
     * @return status service instance
     */
    public static StatusService getStatusService() {
        return statusService;
    }

    /**
     * Obtain reference to web socket server
     * @return web socket server
     */
    public static WebSocketServer getWebSocketServer() {
        return webSocketServer;
    }
}

