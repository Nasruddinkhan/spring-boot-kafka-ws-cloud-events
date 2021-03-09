package com.mypractice.rest.cloud.events.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypractice.rest.cloud.events.dto.User;
import com.mypractice.rest.cloud.events.producer.DynamicProducer;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.Self;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.cloudevents.core.CloudEventUtils.mapData;
@Slf4j
@Path("/")
public class MainResource {
    public static final String NOTIFICATION_EVENT_TYPE = "notification.application";

    private final ObjectMapper objectMapper;
    private final DynamicProducer dynamicProducer;

    @Autowired
    public MainResource(ObjectMapper objectMapper, DynamicProducer dynamicProducer) {
        this.objectMapper = objectMapper;
        this.dynamicProducer = dynamicProducer;
    }

    @POST
    @Path("notification")
    public Response handleNotificationEvent(CloudEvent inputEvent) throws JsonProcessingException {
        if (!inputEvent.getType().equals(NOTIFICATION_EVENT_TYPE)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Event type should be \"" + NOTIFICATION_EVENT_TYPE + "\" but is \"" + inputEvent.getType() + "\"")
                    .build();
        }

        PojoCloudEventData<User> cloudEventData = mapData(inputEvent, PojoCloudEventDataMapper.from(objectMapper, User.class));
        if (cloudEventData == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Event should contain the user")
                    .build();
        }

        User user = cloudEventData.getValue();
        user.setAge(user.getAge() + 1);
        user.setMessage(user.getMessage());
        CloudEvent outputEvent = CloudEventBuilder.from(inputEvent)
                .withData(PojoCloudEventData.wrap(user, objectMapper::writeValueAsBytes))
                .build();
        log.info("outputEvent=> {}", objectMapper.writeValueAsString(outputEvent));
        dynamicProducer.sendMessage(outputEvent, "kafkacloud.in");
        return Response.ok(outputEvent).build();
    }
}
