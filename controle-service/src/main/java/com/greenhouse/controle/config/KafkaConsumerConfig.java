package com.greenhouse.controle.config;

import com.greenhouse.controle.dto.AlertEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration for Controle Service
 * Configures consumer for greenhouse-alerts topic with error handling and DLQ
 */
@Configuration
@EnableKafka
@Slf4j
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.enable-auto-commit:false}")
    private boolean enableAutoCommit;

    @Value("${spring.kafka.consumer.max-poll-records:100}")
    private int maxPollRecords;

    @Value("${spring.kafka.consumer.max-poll-interval-ms:300000}")
    private int maxPollIntervalMs;

    @Value("${spring.kafka.consumer.session-timeout-ms:30000}")
    private int sessionTimeoutMs;

    @Value("${spring.kafka.consumer.heartbeat-interval-ms:10000}")
    private int heartbeatIntervalMs;

    @Value("${kafka.topic.greenhouse-alerts}")
    private String alertsTopic;

    /**
     * Consumer configuration properties
     */
    private Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        
        // Bootstrap servers
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Consumer group
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // Deserialization with error handling
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        
        // JSON Deserializer settings
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AlertEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        
        // Offset management
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        
        // Polling configuration
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        
        // Session and heartbeat
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatIntervalMs);
        
        // Idempotent consumer - ensure exactly once processing
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        log.info("Kafka consumer configured with bootstrap servers: {}, group-id: {}", 
                bootstrapServers, groupId);
        
        return props;
    }

    /**
     * Consumer factory for AlertEvent messages
     */
    @Bean
    public ConsumerFactory<String, AlertEvent> alertConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * Error handler with Dead Letter Queue support
     * Retries 3 times with 1 second interval before sending to DLQ
     */
    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> {
                    log.error("Message failed after retries, sending to DLQ: topic={}, partition={}, offset={}, error={}",
                            record.topic(), record.partition(), record.offset(), exception.getMessage());
                    return new TopicPartition(alertsTopic + ".DLQ", record.partition());
                }
        );

        // Retry 3 times with 1 second backoff
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);
        
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        
        // Don't retry on certain exceptions
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                NullPointerException.class
        );
        
        return errorHandler;
    }

    /**
     * Kafka listener container factory with manual acknowledgment
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AlertEvent> alertKafkaListenerContainerFactory(
            ConsumerFactory<String, AlertEvent> alertConsumerFactory,
            CommonErrorHandler errorHandler) {
        
        ConcurrentKafkaListenerContainerFactory<String, AlertEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(alertConsumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        
        // Manual acknowledgment for exactly-once semantics
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Concurrency - number of consumer threads
        factory.setConcurrency(3);
        
        // Enable batch processing for better throughput
        factory.setBatchListener(false);
        
        log.info("Kafka listener container factory configured with manual acknowledgment");
        
        return factory;
    }
}
