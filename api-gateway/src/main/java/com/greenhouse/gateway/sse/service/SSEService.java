package com.greenhouse.gateway.sse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenhouse.gateway.sse.dto.AlertEvent;
import com.greenhouse.gateway.sse.dto.EquipmentStatusEvent;
import com.greenhouse.gateway.sse.dto.MeasurementEvent;
import com.greenhouse.gateway.sse.dto.SSEMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class SSEService {

    private final ObjectMapper objectMapper;
    
    @Value("${sse.keep-alive-interval:15000}")
    private long keepAliveInterval;
    
    @Value("${sse.max-clients:1000}")
    private int maxClients;
    
    private final AtomicInteger measurementClientCount = new AtomicInteger(0);
    private final AtomicInteger alertClientCount = new AtomicInteger(0);
    private final AtomicInteger equipmentClientCount = new AtomicInteger(0);
    
    // Sinks for broadcasting events to multiple subscribers
    private final Sinks.Many<SSEMessage<MeasurementEvent>> measurementSink;
    private final Sinks.Many<SSEMessage<AlertEvent>> alertSink;
    private final Sinks.Many<SSEMessage<EquipmentStatusEvent>> equipmentStatusSink;
    
    public SSEService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.measurementSink = Sinks.many().multicast().onBackpressureBuffer(256);
        this.alertSink = Sinks.many().multicast().onBackpressureBuffer(256);
        this.equipmentStatusSink = Sinks.many().multicast().onBackpressureBuffer(256);
    }
    
    @PostConstruct
    public void init() {
        log.info("SSE Service initialized with keep-alive interval: {}ms, max clients: {}", 
                keepAliveInterval, maxClients);
    }
    
    /**
     * Get measurement events stream with optional filtering
     */
    public Flux<SSEMessage<?>> getMeasurementStream(String parameterType, String greenhouseId) {
        if (measurementClientCount.get() >= maxClients) {
            return Flux.error(new RuntimeException("Max clients reached for measurement stream"));
        }
        
        Flux<SSEMessage<?>> dataFlux = measurementSink.asFlux()
                .filter(event -> filterMeasurement(event.getData(), parameterType, greenhouseId))
                .map(event -> (SSEMessage<?>) event);
        
        Flux<SSEMessage<?>> keepAliveFlux = Flux.interval(Duration.ofMillis(keepAliveInterval))
                .map(i -> (SSEMessage<?>) SSEMessage.keepAlive());
        
        return Flux.merge(dataFlux, keepAliveFlux)
                .doOnSubscribe(s -> {
                    int count = measurementClientCount.incrementAndGet();
                    log.info("Client subscribed to measurement stream. Total clients: {}", count);
                })
                .doOnCancel(() -> {
                    int count = measurementClientCount.decrementAndGet();
                    log.info("Client unsubscribed from measurement stream. Total clients: {}", count);
                })
                .doOnTerminate(() -> measurementClientCount.decrementAndGet());
    }
    
    /**
     * Get alerts events stream with optional filtering
     */
    public Flux<SSEMessage<?>> getAlertStream(String severity, String greenhouseId) {
        if (alertClientCount.get() >= maxClients) {
            return Flux.error(new RuntimeException("Max clients reached for alert stream"));
        }
        
        Flux<SSEMessage<?>> dataFlux = alertSink.asFlux()
                .filter(event -> filterAlert(event.getData(), severity, greenhouseId))
                .map(event -> (SSEMessage<?>) event);
        
        Flux<SSEMessage<?>> keepAliveFlux = Flux.interval(Duration.ofMillis(keepAliveInterval))
                .map(i -> (SSEMessage<?>) SSEMessage.keepAlive());
        
        return Flux.merge(dataFlux, keepAliveFlux)
                .doOnSubscribe(s -> {
                    int count = alertClientCount.incrementAndGet();
                    log.info("Client subscribed to alert stream. Total clients: {}", count);
                })
                .doOnCancel(() -> {
                    int count = alertClientCount.decrementAndGet();
                    log.info("Client unsubscribed from alert stream. Total clients: {}", count);
                })
                .doOnTerminate(() -> alertClientCount.decrementAndGet());
    }
    
    /**
     * Get equipment status events stream with optional filtering
     */
    public Flux<SSEMessage<?>> getEquipmentStatusStream(String equipmentType, String greenhouseId) {
        if (equipmentClientCount.get() >= maxClients) {
            return Flux.error(new RuntimeException("Max clients reached for equipment status stream"));
        }
        
        Flux<SSEMessage<?>> dataFlux = equipmentStatusSink.asFlux()
                .filter(event -> filterEquipmentStatus(event.getData(), equipmentType, greenhouseId))
                .map(event -> (SSEMessage<?>) event);
        
        Flux<SSEMessage<?>> keepAliveFlux = Flux.interval(Duration.ofMillis(keepAliveInterval))
                .map(i -> (SSEMessage<?>) SSEMessage.keepAlive());
        
        return Flux.merge(dataFlux, keepAliveFlux)
                .doOnSubscribe(s -> {
                    int count = equipmentClientCount.incrementAndGet();
                    log.info("Client subscribed to equipment status stream. Total clients: {}", count);
                })
                .doOnCancel(() -> {
                    int count = equipmentClientCount.decrementAndGet();
                    log.info("Client unsubscribed from equipment status stream. Total clients: {}", count);
                })
                .doOnTerminate(() -> equipmentClientCount.decrementAndGet());
    }
    
    /**
     * Get combined stream for all events
     */
    public Flux<SSEMessage<?>> getCombinedStream(String greenhouseId) {
        Flux<SSEMessage<?>> measurements = measurementSink.asFlux()
                .filter(event -> greenhouseId == null || greenhouseId.equals(event.getData().getGreenhouseId()))
                .map(event -> (SSEMessage<?>) event);
        
        Flux<SSEMessage<?>> alerts = alertSink.asFlux()
                .filter(event -> greenhouseId == null || greenhouseId.equals(event.getData().getGreenhouseId()))
                .map(event -> (SSEMessage<?>) event);
        
        Flux<SSEMessage<?>> equipment = equipmentStatusSink.asFlux()
                .filter(event -> greenhouseId == null || greenhouseId.equals(event.getData().getGreenhouseId()))
                .map(event -> (SSEMessage<?>) event);
        
        Flux<SSEMessage<?>> keepAliveFlux = Flux.interval(Duration.ofMillis(keepAliveInterval))
                .map(i -> (SSEMessage<?>) SSEMessage.keepAlive());
        
        return Flux.merge(measurements, alerts, equipment, keepAliveFlux)
                .doOnSubscribe(s -> log.info("Client subscribed to combined stream"))
                .doOnCancel(() -> log.info("Client unsubscribed from combined stream"));
    }
    
    // Kafka Listeners
    
    @KafkaListener(topics = "measurement-events", groupId = "api-gateway-sse-measurements")
    public void handleMeasurementEvent(Map<String, Object> eventData) {
        try {
            log.debug("Received measurement event from Kafka: {}", eventData);
            
            MeasurementEvent event = MeasurementEvent.builder()
                    .id(getStringValue(eventData, "id", UUID.randomUUID().toString()))
                    .capteurId(getStringValue(eventData, "capteurId", null))
                    .capteurName(getStringValue(eventData, "capteurName", null))
                    .parameterType(getStringValue(eventData, "parameterType", null))
                    .value(getDoubleValue(eventData, "value"))
                    .unit(getStringValue(eventData, "unit", null))
                    .timestamp(LocalDateTime.now())
                    .greenhouseId(getStringValue(eventData, "greenhouseId", null))
                    .greenhouseName(getStringValue(eventData, "greenhouseName", null))
                    .zoneId(getStringValue(eventData, "zoneId", null))
                    .zoneName(getStringValue(eventData, "zoneName", null))
                    .build();
            
            SSEMessage<MeasurementEvent> message = SSEMessage.of("measurement", event);
            measurementSink.tryEmitNext(message);
            
            log.debug("Broadcasted measurement event: {}", message.getEventId());
        } catch (Exception e) {
            log.error("Error processing measurement event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "alert-events", groupId = "api-gateway-sse-alerts")
    public void handleAlertEvent(Map<String, Object> eventData) {
        try {
            log.debug("Received alert event from Kafka: {}", eventData);
            
            AlertEvent event = AlertEvent.builder()
                    .id(getStringValue(eventData, "id", UUID.randomUUID().toString()))
                    .type(getStringValue(eventData, "type", null))
                    .severity(getStringValue(eventData, "severity", "WARNING"))
                    .message(getStringValue(eventData, "message", null))
                    .source(getStringValue(eventData, "source", null))
                    .sourceId(getStringValue(eventData, "sourceId", null))
                    .parameterType(getStringValue(eventData, "parameterType", null))
                    .currentValue(getDoubleValue(eventData, "currentValue"))
                    .thresholdValue(getDoubleValue(eventData, "thresholdValue"))
                    .greenhouseId(getStringValue(eventData, "greenhouseId", null))
                    .greenhouseName(getStringValue(eventData, "greenhouseName", null))
                    .timestamp(LocalDateTime.now())
                    .acknowledged(getBooleanValue(eventData, "acknowledged", false))
                    .build();
            
            SSEMessage<AlertEvent> message = SSEMessage.of("alert", event);
            alertSink.tryEmitNext(message);
            
            log.info("Broadcasted alert event: {} - {}", event.getSeverity(), event.getMessage());
        } catch (Exception e) {
            log.error("Error processing alert event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "equipment-status-events", groupId = "api-gateway-sse-equipment")
    public void handleEquipmentStatusEvent(Map<String, Object> eventData) {
        try {
            log.debug("Received equipment status event from Kafka: {}", eventData);
            
            EquipmentStatusEvent event = EquipmentStatusEvent.builder()
                    .id(getStringValue(eventData, "id", UUID.randomUUID().toString()))
                    .equipmentId(getStringValue(eventData, "equipmentId", null))
                    .equipmentName(getStringValue(eventData, "equipmentName", null))
                    .equipmentType(getStringValue(eventData, "equipmentType", null))
                    .status(getStringValue(eventData, "status", null))
                    .previousStatus(getStringValue(eventData, "previousStatus", null))
                    .greenhouseId(getStringValue(eventData, "greenhouseId", null))
                    .greenhouseName(getStringValue(eventData, "greenhouseName", null))
                    .zoneId(getStringValue(eventData, "zoneId", null))
                    .zoneName(getStringValue(eventData, "zoneName", null))
                    .timestamp(LocalDateTime.now())
                    .triggeredBy(getStringValue(eventData, "triggeredBy", null))
                    .build();
            
            SSEMessage<EquipmentStatusEvent> message = SSEMessage.of("equipment-status", event);
            equipmentStatusSink.tryEmitNext(message);
            
            log.debug("Broadcasted equipment status event: {}", message.getEventId());
        } catch (Exception e) {
            log.error("Error processing equipment status event: {}", e.getMessage(), e);
        }
    }
    
    // Helper methods for filtering
    
    private boolean filterMeasurement(MeasurementEvent event, String parameterType, String greenhouseId) {
        if (event == null) return false;
        if (parameterType != null && !parameterType.equalsIgnoreCase(event.getParameterType())) return false;
        if (greenhouseId != null && !greenhouseId.equals(event.getGreenhouseId())) return false;
        return true;
    }
    
    private boolean filterAlert(AlertEvent event, String severity, String greenhouseId) {
        if (event == null) return false;
        if (severity != null && !severity.equalsIgnoreCase(event.getSeverity())) return false;
        if (greenhouseId != null && !greenhouseId.equals(event.getGreenhouseId())) return false;
        return true;
    }
    
    private boolean filterEquipmentStatus(EquipmentStatusEvent event, String equipmentType, String greenhouseId) {
        if (event == null) return false;
        if (equipmentType != null && !equipmentType.equalsIgnoreCase(event.getEquipmentType())) return false;
        if (greenhouseId != null && !greenhouseId.equals(event.getGreenhouseId())) return false;
        return true;
    }
    
    // Helper methods for safe value extraction from Map
    
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Boolean getBooleanValue(Map<String, Object> map, String key, Boolean defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }
    
    // Statistics methods
    
    public int getMeasurementClientCount() {
        return measurementClientCount.get();
    }
    
    public int getAlertClientCount() {
        return alertClientCount.get();
    }
    
    public int getEquipmentClientCount() {
        return equipmentClientCount.get();
    }
    
    public int getTotalClientCount() {
        return measurementClientCount.get() + alertClientCount.get() + equipmentClientCount.get();
    }
}
