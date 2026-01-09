package com.greenhouse.environnement.config;

import com.greenhouse.environnement.dto.AlertEvent;
import com.greenhouse.environnement.dto.MeasurementEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer Configuration for Environnement Service
 * Configures producers for greenhouse-alerts and measurement-stream topics
 */
@Configuration
@Slf4j
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks:all}")
    private String acks;

    @Value("${spring.kafka.producer.retries:3}")
    private int retries;

    @Value("${spring.kafka.producer.properties.retry.backoff.ms:1000}")
    private int retryBackoffMs;

    @Value("${spring.kafka.producer.properties.request.timeout.ms:30000}")
    private int requestTimeoutMs;

    @Value("${spring.kafka.producer.properties.delivery.timeout.ms:120000}")
    private int deliveryTimeoutMs;

    @Value("${spring.kafka.producer.properties.linger.ms:5}")
    private int lingerMs;

    @Value("${spring.kafka.producer.properties.batch.size:16384}")
    private int batchSize;

    @Value("${spring.kafka.producer.properties.buffer.memory:33554432}")
    private int bufferMemory;

    /**
     * Common producer configuration
     */
    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        
        // Bootstrap servers
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Serialization
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);
        
        // Timeout settings
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
        
        // Performance tuning
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        
        // Idempotence - ensures exactly-once semantics
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Max in-flight requests for ordering guarantee with idempotence
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        // Compression - using gzip instead of snappy (snappy requires glibc, Alpine uses musl)
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        
        // JSON serializer settings
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        
        log.info("Kafka producer configured with bootstrap servers: {}", bootstrapServers);
        
        return props;
    }

    /**
     * Producer factory for AlertEvent messages
     */
    @Bean
    public ProducerFactory<String, AlertEvent> alertProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * KafkaTemplate for sending AlertEvent messages
     */
    @Bean
    public KafkaTemplate<String, AlertEvent> alertKafkaTemplate() {
        KafkaTemplate<String, AlertEvent> template = new KafkaTemplate<>(alertProducerFactory());
        template.setObservationEnabled(true);
        return template;
    }

    /**
     * Producer factory for MeasurementEvent messages
     */
    @Bean
    public ProducerFactory<String, MeasurementEvent> measurementProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * KafkaTemplate for sending MeasurementEvent messages
     */
    @Bean
    public KafkaTemplate<String, MeasurementEvent> measurementKafkaTemplate() {
        KafkaTemplate<String, MeasurementEvent> template = new KafkaTemplate<>(measurementProducerFactory());
        template.setObservationEnabled(true);
        return template;
    }

    /**
     * Producer factory for generic Object messages (used for health checks)
     */
    @Bean
    public ProducerFactory<String, Object> genericProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * Generic KafkaTemplate for health checks and general purpose
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(genericProducerFactory());
    }
}
