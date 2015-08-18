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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
        /*Status postResult = statusAPI.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(status, MediaType.APPLICATION_JSON_TYPE), Status.class);*/
        statusAPI.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(status, MediaType.APPLICATION_JSON_TYPE), Status.class);
    }

    public void sendReport(Report report) {
        reportAPI.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(report, MediaType.APPLICATION_JSON_TYPE), Report.class);
    }

    public static void main(String[] args) {
        DeathStarClient client = new DeathStarClient("localhost", 8000);

        Report report = new Report(152, "command", "REMOVED", "hostABCC", Instant.now().toString());
        Status status1 = new Status(399, "localhost", Instant.now().toString());
        Status status2 = new Status(401, "hostabed", Instant.now().toString());

        long start = System.nanoTime();
        client.sendReport(report);
        //client.sendStatus(status1);
        //client.sendStatus(status2);

        statusTesting(client);

        long elapsed = System.nanoTime() - start;

        System.out.println("Took: " + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS) + " ms");
    }


    // Sends status updates for numerous mock x-wings
    public static void statusTesting(DeathStarClient c) {

        DeathStarClient client = c;

        int i = 0;

        Status status1 = new Status(399, "localhost", Instant.now().toString());
        Status status2 = new Status(401, "hostabed", Instant.now().toString());
        ArrayList<Status> statuses = new ArrayList<Status>();
        statuses.add(status1);
        statuses.add(status2);

        // 10 iterations
        while (i < 6){

            // every 2 iterations, create new status, aka new x-wing
            if ((i % 2) == 0)
            {
                Status s = new Status((int)(Math.random() * 5000), "localhost", Instant.now().toString());
                statuses.add(s);
            }

            // try to delay the last pass by 10 seconds
            if (i == 5)
            {
                // sleep for 10 seconds
                try {
                    Thread.sleep(10000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            // send all statuses but update the timestamp
            for (int j=0; j < statuses.size(); j++)
            {
                Status s = new Status(statuses.get(j).getXWingId(), statuses.get(j).host, Instant.now().toString());

                client.sendStatus(s);
            }

            // sleep for 2 seconds
            try {
                Thread.sleep(2000);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            i++;
        }
    }

}
