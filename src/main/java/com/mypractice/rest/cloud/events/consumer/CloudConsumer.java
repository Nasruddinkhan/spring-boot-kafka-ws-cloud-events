package com.mypractice.rest.cloud.events.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypractice.rest.cloud.events.dto.User;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static io.cloudevents.core.CloudEventUtils.mapData;


@Component
@Slf4j
public class CloudConsumer {
    private final ObjectMapper objectMapper;
    @Autowired
    public CloudConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @KafkaListener(topics = "kafkacloud.in", groupId = "kafka-cloud")
    public void consume(@Payload CloudEvent message, Acknowledgment acknowledgment) throws ClassNotFoundException {
        PojoCloudEventData<User> cloudEventData = mapData(message, PojoCloudEventDataMapper.from(objectMapper, User.class));
        log.info("payload {}",cloudEventData.getValue());
            acknowledgment.acknowledge();
        }
}
