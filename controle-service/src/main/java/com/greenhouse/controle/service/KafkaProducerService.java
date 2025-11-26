package com.greenhouse.controle.service;

import com.greenhouse.controle.dto.EquipmentActionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer Service for Controle Service
 * Publishes to:
 * - equipment-actions: Equipment action notifications for notification service and dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, EquipmentActionEvent> equipmentActionKafkaTemplate;

    @Value("${kafka.topic.equipment-actions}")
    private String equipmentActionsTopic;

    /**
     * Send equipment action event to equipment-actions topic
     * Uses equipementId as the message key for partitioning
     */
    public void sendEquipmentAction(EquipmentActionEvent actionEvent) {
        String key = actionEvent.getEquipementId().toString();
        
        log.info("Sending equipment action to topic '{}' with key '{}': actionId={}, type={}, status={}, eventId={}",
                equipmentActionsTopic, key, actionEvent.getActionId(), 
                actionEvent.getTypeAction(), actionEvent.getStatut(), actionEvent.getEventId());

        CompletableFuture<SendResult<String, EquipmentActionEvent>> future = 
                equipmentActionKafkaTemplate.send(equipmentActionsTopic, key, actionEvent);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Equipment action sent successfully to topic '{}', partition={}, offset={}, eventId={}",
                        equipmentActionsTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        actionEvent.getEventId());
            } else {
                log.error("Failed to send equipment action to topic '{}': eventId={}, error={}",
                        equipmentActionsTopic, actionEvent.getEventId(), ex.getMessage(), ex);
                // Send to DLQ on failure
                sendToEquipmentActionDlq(actionEvent, ex.getMessage());
            }
        });
    }

    /**
     * Send equipment action synchronously - use when you need to ensure delivery
     */
    public boolean sendEquipmentActionSync(EquipmentActionEvent actionEvent) {
        String key = actionEvent.getEquipementId().toString();
        
        try {
            log.info("Sending equipment action synchronously to topic '{}': eventId={}", 
                    equipmentActionsTopic, actionEvent.getEventId());
            
            SendResult<String, EquipmentActionEvent> result = 
                    equipmentActionKafkaTemplate.send(equipmentActionsTopic, key, actionEvent).get();
            
            log.info("Equipment action sent synchronously: topic={}, partition={}, offset={}, eventId={}",
                    equipmentActionsTopic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    actionEvent.getEventId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send equipment action synchronously: eventId={}, error={}",
                    actionEvent.getEventId(), e.getMessage(), e);
            sendToEquipmentActionDlq(actionEvent, e.getMessage());
            return false;
        }
    }

    /**
     * Send failed equipment action to Dead Letter Queue
     */
    private void sendToEquipmentActionDlq(EquipmentActionEvent actionEvent, String errorMessage) {
        String dlqTopic = equipmentActionsTopic + ".DLQ";
        String key = actionEvent.getEquipementId().toString();
        
        log.warn("Sending equipment action to DLQ '{}': eventId={}, originalError={}",
                dlqTopic, actionEvent.getEventId(), errorMessage);
        
        try {
            equipmentActionKafkaTemplate.send(dlqTopic, key, actionEvent);
            log.info("Equipment action successfully sent to DLQ: eventId={}", actionEvent.getEventId());
        } catch (Exception e) {
            log.error("Failed to send equipment action to DLQ: eventId={}, error={}",
                    actionEvent.getEventId(), e.getMessage(), e);
        }
    }
}
