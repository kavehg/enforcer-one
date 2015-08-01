package io.enforcer.deathstar;

import io.enforcer.deathstar.pojos.Report;
import io.enforcer.deathstar.pojos.Status;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by kavehg on 7/13/2015.
 */
public class DeathStarClient {

    private final String deathStarHost;
    private final Integer deathStarPort;
    private final Client client;
    private final WebTarget statusAPI;
    private final WebTarget reportAPI;

    public DeathStarClient(String host, Integer port) {
        this.deathStarHost = host;
        this.deathStarPort = port;

        final Map<String, String> namespacePrefixMapper = new HashMap<String, String>();
        namespacePrefixMapper.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

        final MoxyJsonConfig moxyJsonConfig = new MoxyJsonConfig()
                .setNamespacePrefixMapper(namespacePrefixMapper)
                .setNamespaceSeparator(':');

        final ContextResolver<MoxyJsonConfig> jsonConfigResolver = moxyJsonConfig.resolver();

        client = ClientBuilder.newBuilder()
                .register(MoxyJsonFeature.class)
                .register(jsonConfigResolver)
                .build();

        statusAPI = client.target("http://" + deathStarHost + ":" + deathStarPort + "/api/status");
        reportAPI = client.target("http://" + deathStarHost + ":" + deathStarPort + "/api/reports");
    }

    public void sendStatus(Status status) {
        Status postResult = statusAPI.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(status, MediaType.APPLICATION_JSON_TYPE), Status.class);
    }

    public void sendReport(Report report) {
        reportAPI.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(report, MediaType.APPLICATION_JSON_TYPE), Report.class);
    }

    public static void main(String[] args) {
        DeathStarClient client = new DeathStarClient("localhost", 8080);
        Status status = new Status(1, "host", "2015-07-12T21:45:32Z");
        Report report = new Report(1, "command", "ADDED", "hostABC", "2015-07-29T00:00:00Z");
        long start = System.nanoTime();
//        client.sendStatus(status);
        client.sendReport(report);
        long elapsed = System.nanoTime() - start;
        System.out.println("Took: " + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS) + " ms");
    }
}
