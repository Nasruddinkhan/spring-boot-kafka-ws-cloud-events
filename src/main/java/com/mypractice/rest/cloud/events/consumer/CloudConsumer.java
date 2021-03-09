package com.mypractice.rest.cloud.events.consumer;

import com.mypractice.rest.cloud.events.dto.User;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.data.PojoCloudEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class CloudConsumer {
    @KafkaListener(topics = "kafkacloud.in", groupId = "kafka-cloud")
    public void consume(@Payload User message, Acknowledgment acknowledgment) throws ClassNotFoundException {
        log.info("payload {}",message);
            acknowledgment.acknowledge();
            log.info("payload {}",message);
        }
}
