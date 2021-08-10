package org.acme;

import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Set;

@Path("/")
public class GreetingResource {

    @ConfigProperty(name = "latency")
    int latency;

    @POST
    @Blocking
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes()
    public String form(@Context HttpHeaders httpHeaders, String body) throws InterruptedException {

        final StringBuilder builder = new StringBuilder();
        final Set<String> headerKeys = httpHeaders.getRequestHeaders().keySet();

        builder.append("\nHeaders:\n");
        for (String headerKey : headerKeys) {
            String headerValue = httpHeaders.getRequestHeader(headerKey).get(0);
            builder.append("  ").append(headerKey).append(":").append(headerValue).append("\n");
        }

        builder.append("Body:\n  ").append(body).append("\n");

        System.out.println(builder);

        Thread.sleep(latency);

        return "{\"ok\": \"ok\"}";
    }

}
