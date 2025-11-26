package com.greenhouse.controle.config;

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
 * Kafka Topic Configuration for Controle Service
 * Creates topics programmatically with proper partitions and replication
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.greenhouse-alerts}")
    private String alertsTopic;

    @Value("${kafka.topic.equipment-actions}")
    private String equipmentActionsTopic;

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
     * Topic for equipment action notifications
     * Consumed by: Notification service (future), Dashboard (SSE)
     */
    @Bean
    public NewTopic equipmentActionsTopic() {
        return TopicBuilder.name(equipmentActionsTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000") // 7 days retention
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", "1")
                .build();
    }

    /**
     * Dead Letter Queue for failed alert processing
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
     * Dead Letter Queue for failed equipment action messages
     */
    @Bean
    public NewTopic equipmentActionsDlqTopic() {
        return TopicBuilder.name(equipmentActionsTopic + ".DLQ")
                .partitions(1)
                .replicas(replicationFactor)
                .config("retention.ms", "2592000000") // 30 days retention
                .build();
    }
}
