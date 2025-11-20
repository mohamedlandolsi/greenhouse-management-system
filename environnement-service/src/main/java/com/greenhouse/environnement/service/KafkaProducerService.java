package com.greenhouse.environnement.service;

import com.greenhouse.environnement.dto.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, AlertEvent> kafkaTemplate;
    private static final String TOPIC = "greenhouse-alerts";

    public void sendAlert(AlertEvent alertEvent) {
        try {
            log.info("Sending alert to Kafka topic {}: {}", TOPIC, alertEvent);
            kafkaTemplate.send(TOPIC, alertEvent.getParametreId().toString(), alertEvent);
            log.info("Alert sent successfully");
        } catch (Exception e) {
            log.error("Failed to send alert to Kafka: {}", e.getMessage(), e);
        }
    }
}
