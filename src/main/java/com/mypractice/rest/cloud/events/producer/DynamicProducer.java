package com.mypractice.rest.cloud.events.producer;

import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DynamicProducer {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;

    @Autowired
    DynamicProducer(KafkaTemplate<String, CloudEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(CloudEvent message, String topicName) {
        this.kafkaTemplate.send(topicName, message)
                .addCallback(result -> {
                    log.info("Sent message: " + message
                            + " with offset: " + result.getRecordMetadata().offset());
                }, ex -> {
                    log.error("Unable to send message : " + message, ex);
                });
    }
}
