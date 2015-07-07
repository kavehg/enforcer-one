package io.enforcer.deathstar;

import io.enforcer.deathstar.services.StatusService;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * DeathStar class
 */
public class DeathStar {

    private static final Logger logger = Logger.getLogger(DeathStar.class.getName());

    // Base URI the Grizzly HTTP server will listen on
    public static final String API_BASE_URI = "http://localhost:8080/api/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() throws IOException {
        // create a resource config that scans for JAX-RS resources and providers
        // in io.enforcer.deathstar package
        final ResourceConfig rc = new ResourceConfig().packages("io.enforcer.deathstar.rest");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at API_BASE_URI
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(API_BASE_URI), rc);

        // serve static content from the resources/www directory
        HttpHandler httpHandler = new CLStaticHttpHandler(HttpServer.class.getClassLoader(), "www/");
        httpServer.getServerConfiguration().addHttpHandler(httpHandler, "/");

        return httpServer;
    }

    /**
     * DeathStar method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(
                String.format("REST api started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop service...", API_BASE_URI));
        System.in.read();
        server.stop();
        StatusService.stopService();
    }
}

