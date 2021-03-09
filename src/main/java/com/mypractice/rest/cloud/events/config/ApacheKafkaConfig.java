package com.mypractice.rest.cloud.events.config;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.Encoding;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.kafka.CloudEventDeserializer;
import io.cloudevents.kafka.CloudEventSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;


@Slf4j
@Configuration
public class ApacheKafkaConfig {
    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CloudEventDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }

    @Bean
    public ConsumerFactory consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                new CloudEventDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CloudEventDeserializer> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CloudEventDeserializer> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setRetryTemplate(kafkaRetry());
        factory.setConsumerFactory(consumerFactory());
        factory.setErrorHandler(((exception, data) -> {
            /* here you can do you custom handling, I am just logging it same as default Error handler does
If you just want to log. you need not configure the error handler here. The default handler does it for you.
Generally, you will persist the failed records to DB for tracking the failed records.  */
            log.error("Error in process with Exception {} and the record is {}", exception, data);
        }));
        factory.setRecoveryCallback((context -> {
            if(context.getLastThrowable().getCause() instanceof RecoverableDataAccessException){
                //here you can do your recovery mechanism where you can put back on to the topic using a Kafka producer
            } else{
                // here you can log things and throw some custom exception that Error handler will take care of ..
                throw new RuntimeException(context.getLastThrowable().getMessage());
            }
            return null;

        }));
        return factory;
    }

    public RetryTemplate kafkaRetry() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(getSimpleRetryPolicy());
        return retryTemplate;
    }

    private SimpleRetryPolicy getSimpleRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<>();
        exceptionMap.put(IllegalArgumentException.class, false);
        exceptionMap.put(TimeoutException.class, true);

        return new SimpleRetryPolicy(3,exceptionMap,true);
    }


    @Bean
    public ProducerFactory userProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CloudEventSerializer.class);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        configProps.put(CloudEventSerializer.ENCODING_CONFIG, Encoding.BINARY);
        configProps.put(CloudEventSerializer.EVENT_FORMAT_CONFIG, JsonFormat.CONTENT_TYPE);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, CloudEvent> userKafkaTemplate() {
        return new KafkaTemplate(userProducerFactory());
    }
}
