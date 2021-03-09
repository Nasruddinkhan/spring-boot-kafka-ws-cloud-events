package com.mypractice.rest.cloud.events.config;


import com.mypractice.rest.cloud.events.rest.MainResource;
import io.cloudevents.http.restful.ws.CloudEventsProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JerseyConfiguration extends ResourceConfig {

    public JerseyConfiguration() {
        registerClasses(CloudEventsProvider.class, MainResource.class);
    }

}
