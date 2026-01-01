package com.greenhouse.environnement.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Custom health indicator that checks Kafka broker connectivity.
 */
@Component
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaHealthIndicator(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Health health() {
        try {
            AdminClient adminClient = AdminClient.create(
                kafkaTemplate.getProducerFactory().getConfigurationProperties()
            );
            
            // Try to describe cluster with 5 second timeout
            adminClient.describeCluster(new DescribeClusterOptions().timeoutMs(5000))
                .clusterId()
                .get(5, TimeUnit.SECONDS);
            
            adminClient.close();
            
            return Health.up()
                .withDetail("status", "Kafka broker is reachable")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Kafka broker unreachable: " + e.getMessage())
                .build();
        }
    }
}
