package com.mypractice.rest.cloud.events.producer;

import com.mypractice.rest.cloud.events.dto.User;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
@Slf4j
@Component
public class DynamicProducer {

    private final KafkaTemplate<String, User> kafkaTemplate;

    @Autowired
    DynamicProducer(KafkaTemplate<String, User> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(User message, String topicName) {
        ListenableFuture<SendResult<String, User>> future = this.kafkaTemplate.send(topicName, message);
        future.addCallback(new ListenableFutureCallback<SendResult<String, User>>() {
            @Override
            public void onSuccess(SendResult<String, User> result) {
                log.info("Sent message: " + message
                        + " with offset: " + result.getRecordMetadata().offset());
            }
            @Override
            public void onFailure(Throwable ex) {
                log.error("Unable to send message : " + message, ex);
            }
        });
    }
}
