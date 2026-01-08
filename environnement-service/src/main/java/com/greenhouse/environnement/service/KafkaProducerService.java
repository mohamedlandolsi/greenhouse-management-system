package com.greenhouse.environnement.service;

import com.greenhouse.environnement.dto.AlertEvent;
import com.greenhouse.environnement.dto.MeasurementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer Service for Environnement Service
 * Publishes to:
 * - greenhouse-alerts: Environmental threshold violation alerts
 * - measurement-stream: Real-time measurements for analytics and dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, AlertEvent> alertKafkaTemplate;
    private final KafkaTemplate<String, MeasurementEvent> measurementKafkaTemplate;

    @Value("${kafka.topic.greenhouse-alerts}")
    private String alertsTopic;

    @Value("${kafka.topic.measurement-stream}")
    private String measurementTopic;

    /**
     * Send alert event to greenhouse-alerts topic
     * Uses parametreId as the message key for partitioning
     */
    public void sendAlert(AlertEvent alertEvent) {
        String key = alertEvent.getParametreId().toString();
        
        log.info("Sending alert to topic '{}' with key '{}': type={}, value={}, eventId={}",
                alertsTopic, key, alertEvent.getParametreType(), alertEvent.getValeur(), alertEvent.getEventId());

        CompletableFuture<SendResult<String, AlertEvent>> future = 
                alertKafkaTemplate.send(alertsTopic, key, alertEvent);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Alert sent successfully to topic '{}', partition={}, offset={}, eventId={}",
                        alertsTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        alertEvent.getEventId());
            } else {
                log.error("Failed to send alert to topic '{}': eventId={}, error={}",
                        alertsTopic, alertEvent.getEventId(), ex.getMessage(), ex);
                // Here you could implement retry logic or send to DLQ
                sendToAlertDlq(alertEvent, ex.getMessage());
            }
        });
    }

    /**
     * Send measurement event to measurement-stream topic
     * Uses parametreId as the message key for partitioning
     */
    public void sendMeasurement(MeasurementEvent measurementEvent) {
        String key = measurementEvent.getParametreId().toString();
        
        log.debug("Sending measurement to topic '{}' with key '{}': type={}, value={}, eventId={}",
                measurementTopic, key, measurementEvent.getParametreType(), measurementEvent.getValeur(), 
                measurementEvent.getEventId());

        CompletableFuture<SendResult<String, MeasurementEvent>> future = 
                measurementKafkaTemplate.send(measurementTopic, key, measurementEvent);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Measurement sent successfully to topic '{}', partition={}, offset={}, eventId={}",
                        measurementTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        measurementEvent.getEventId());
            } else {
                log.error("Failed to send measurement to topic '{}': eventId={}, error={}",
                        measurementTopic, measurementEvent.getEventId(), ex.getMessage(), ex);
                // Measurements are less critical, just log the error
            }
        });
    }

    /**
     * Send alert synchronously - use when you need to ensure delivery
     */
    public boolean sendAlertSync(AlertEvent alertEvent) {
        String key = alertEvent.getParametreId().toString();
        
        try {
            log.info("Sending alert synchronously to topic '{}': eventId={}", 
                    alertsTopic, alertEvent.getEventId());
            
            SendResult<String, AlertEvent> result = 
                    alertKafkaTemplate.send(alertsTopic, key, alertEvent).get();
            
            log.info("Alert sent synchronously: topic={}, partition={}, offset={}, eventId={}",
                    alertsTopic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    alertEvent.getEventId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send alert synchronously: eventId={}, error={}",
                    alertEvent.getEventId(), e.getMessage(), e);
            sendToAlertDlq(alertEvent, e.getMessage());
            return false;
        }
    }

    /**
     * Send failed alert to Dead Letter Queue
     */
    private void sendToAlertDlq(AlertEvent alertEvent, String errorMessage) {
        String dlqTopic = alertsTopic + ".DLQ";
        String key = alertEvent.getParametreId().toString();
        
        log.warn("Sending alert to DLQ '{}': eventId={}, originalError={}",
                dlqTopic, alertEvent.getEventId(), errorMessage);
        
        try {
            alertKafkaTemplate.send(dlqTopic, key, alertEvent);
            log.info("Alert successfully sent to DLQ: eventId={}", alertEvent.getEventId());
        } catch (Exception e) {
            log.error("Failed to send alert to DLQ: eventId={}, error={}",
                    alertEvent.getEventId(), e.getMessage(), e);
        }
    }
}
