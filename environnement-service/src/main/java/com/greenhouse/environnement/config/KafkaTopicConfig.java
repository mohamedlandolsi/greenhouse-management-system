package com.greenhouse.environnement.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Topic Configuration for Environnement Service
 * Creates topics programmatically with proper partitions and replication
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.greenhouse-alerts}")
    private String alertsTopic;

    @Value("${kafka.topic.measurement-stream}")
    private String measurementTopic;

    @Value("${kafka.topic.partitions:3}")
    private int partitions;

    @Value("${kafka.topic.replication-factor:1}")
    private int replicationFactor;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Topic for environmental threshold violation alerts
     * Consumed by: Contrôle service
     */
    @Bean
    public NewTopic greenhouseAlertsTopic() {
        return TopicBuilder.name(alertsTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000") // 7 days retention
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", "1")
                .build();
    }

    /**
     * Topic for real-time measurement streaming
     * Consumed by: Analytics service (future), Dashboard (SSE)
     */
    @Bean
    public NewTopic measurementStreamTopic() {
        return TopicBuilder.name(measurementTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "86400000") // 1 day retention
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", "1")
                .build();
    }

    /**
     * Dead Letter Queue for failed alert messages
     */
    @Bean
    public NewTopic alertsDlqTopic() {
        return TopicBuilder.name(alertsTopic + ".DLQ")
                .partitions(1)
                .replicas(replicationFactor)
                .config("retention.ms", "2592000000") // 30 days retention
                .build();
    }

    /**
     * Dead Letter Queue for failed measurement messages
     */
    @Bean
    public NewTopic measurementDlqTopic() {
        return TopicBuilder.name(measurementTopic + ".DLQ")
                .partitions(1)
                .replicas(replicationFactor)
                .config("retention.ms", "2592000000") // 30 days retention
                .build();
    }
}
