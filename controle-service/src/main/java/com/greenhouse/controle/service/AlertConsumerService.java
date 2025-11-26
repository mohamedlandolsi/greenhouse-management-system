package com.greenhouse.controle.service;

import com.greenhouse.controle.dto.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka Consumer Service for processing environmental alerts
 * Consumes from: greenhouse-alerts topic
 * Implements idempotent processing to handle duplicate messages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertConsumerService {

    private final ActionService actionService;
    
    // In-memory cache for idempotency (in production, use Redis or database)
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();
    
    // Cache size limit to prevent memory issues
    private static final int MAX_CACHE_SIZE = 10000;

    /**
     * Consume alerts from greenhouse-alerts topic
     * Uses manual acknowledgment for exactly-once processing semantics
     */
    @KafkaListener(
            topics = "${kafka.topic.greenhouse-alerts}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "alertKafkaListenerContainerFactory"
    )
    public void consumeAlert(ConsumerRecord<String, AlertEvent> record, Acknowledgment acknowledgment) {
        AlertEvent alert = record.value();
        
        log.info("Received alert from topic '{}': partition={}, offset={}, key={}, eventId={}",
                record.topic(), record.partition(), record.offset(), record.key(), 
                alert != null ? alert.getEventId() : "null");
        
        try {
            // Validate alert
            if (alert == null) {
                log.warn("Received null alert, acknowledging and skipping");
                acknowledgment.acknowledge();
                return;
            }

            // Idempotency check - skip if already processed
            if (isAlreadyProcessed(alert.getEventId())) {
                log.info("Alert already processed, skipping: eventId={}", alert.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            // Process the alert
            processAlert(alert);
            
            // Mark as processed
            markAsProcessed(alert.getEventId());
            
            // Acknowledge after successful processing
            acknowledgment.acknowledge();
            
            log.info("Alert processed and acknowledged successfully: eventId={}", alert.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process alert: eventId={}, error={}", 
                    alert != null ? alert.getEventId() : "unknown", e.getMessage(), e);
            // Don't acknowledge - message will be retried or sent to DLQ by error handler
            throw e;
        }
    }

    /**
     * Process the alert by creating an automatic action
     */
    private void processAlert(AlertEvent alert) {
        log.info("Processing alert: type={}, value={}, thresholds=[{}-{}], severity={}",
                alert.getParametreType(), alert.getValeur(), 
                alert.getSeuilMin(), alert.getSeuilMax(), alert.getSeverity());
        
        // Create automatic corrective action
        actionService.createAutomaticAction(alert);
        
        log.info("Automatic action created for alert: eventId={}", alert.getEventId());
    }

    /**
     * Check if event has already been processed (idempotency)
     */
    private boolean isAlreadyProcessed(String eventId) {
        if (eventId == null) {
            return false;
        }
        return processedEventIds.contains(eventId);
    }

    /**
     * Mark event as processed (idempotency)
     */
    private void markAsProcessed(String eventId) {
        if (eventId == null) {
            return;
        }
        
        // Prevent unbounded growth of the cache
        if (processedEventIds.size() >= MAX_CACHE_SIZE) {
            log.warn("Processed events cache reached max size ({}), clearing oldest entries", MAX_CACHE_SIZE);
            // In production, use a proper LRU cache or Redis with TTL
            processedEventIds.clear();
        }
        
        processedEventIds.add(eventId);
    }

    /**
     * Get count of processed events (for monitoring)
     */
    public int getProcessedEventsCount() {
        return processedEventIds.size();
    }
}
