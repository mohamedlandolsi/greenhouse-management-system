package com.greenhouse.controle.service;

import com.greenhouse.controle.dto.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertConsumerService {

    private final ActionService actionService;

    @KafkaListener(topics = "${spring.kafka.topic.greenhouse-alerts}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAlert(AlertEvent alert) {
        log.info("Received alert for parameter type: {}, value: {}", alert.getParametreType(), alert.getValeur());
        
        try {
            // Create automatic corrective action
            actionService.createAutomaticAction(alert);
            log.info("Automatic action created successfully for alert");
        } catch (Exception e) {
            log.error("Failed to create automatic action for alert: {}", e.getMessage(), e);
        }
    }
}
