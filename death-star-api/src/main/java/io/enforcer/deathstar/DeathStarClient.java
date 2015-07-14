package io.enforcer.deathstar;

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

/**
 * Created by ghahrk2 on 7/13/2015.
 */
public class DeathStarClient {

    private String targetURL;

    public DeathStarClient(String url) {
        this.targetURL = url;
    }

    public void sendStatus(Status status) {
        final Map<String, String> namespacePrefixMapper = new HashMap<String, String>();
        namespacePrefixMapper.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

        final MoxyJsonConfig moxyJsonConfig = new MoxyJsonConfig()
                .setNamespacePrefixMapper(namespacePrefixMapper)
                .setNamespaceSeparator(':');

        final ContextResolver<MoxyJsonConfig> jsonConfigResolver = moxyJsonConfig.resolver();

        final Client client = ClientBuilder.newBuilder()
                .register(MoxyJsonFeature.class)
                .register(jsonConfigResolver)
                .build();

        WebTarget webTarget = client.target(targetURL + "/status");
        Status postResult = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(status, MediaType.APPLICATION_JSON_TYPE), Status.class);


    }

    public static void main(String[] args) {
        DeathStarClient client = new DeathStarClient("http://localhost:8080/api");
        Status status = new Status(1, "host", "2015-07-12T21:45:32Z");
        client.sendStatus(status);
    }
}
