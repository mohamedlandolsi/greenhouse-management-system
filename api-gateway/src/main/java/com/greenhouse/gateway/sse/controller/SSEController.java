package com.greenhouse.gateway.sse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenhouse.gateway.sse.dto.SSEMessage;
import com.greenhouse.gateway.sse.service.SSEService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class SSEController {

    private final SSEService sseService;
    private final ObjectMapper objectMapper;

    /**
     * Stream measurement events via SSE
     * 
     * @param parameterType Optional filter by parameter type (temperature, humidity, light, etc.)
     * @param greenhouseId Optional filter by greenhouse ID
     * @return Flux of SSE events
     */
    @GetMapping(value = "/measurements", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamMeasurements(
            @RequestParam(required = false) String parameterType,
            @RequestParam(required = false) String greenhouseId) {
        
        log.info("New SSE connection for measurements - parameterType: {}, greenhouseId: {}", 
                parameterType, greenhouseId);
        
        return sseService.getMeasurementStream(parameterType, greenhouseId)
                .map(this::toServerSentEvent)
                .onErrorResume(e -> {
                    log.error("Error in measurement stream: {}", e.getMessage());
                    return Flux.just(createErrorEvent(e.getMessage()));
                });
    }

    /**
     * Stream alert events via SSE
     * 
     * @param severity Optional filter by severity (INFO, WARNING, CRITICAL)
     * @param greenhouseId Optional filter by greenhouse ID
     * @return Flux of SSE events
     */
    @GetMapping(value = "/alerts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamAlerts(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String greenhouseId) {
        
        log.info("New SSE connection for alerts - severity: {}, greenhouseId: {}", 
                severity, greenhouseId);
        
        return sseService.getAlertStream(severity, greenhouseId)
                .map(this::toServerSentEvent)
                .onErrorResume(e -> {
                    log.error("Error in alert stream: {}", e.getMessage());
                    return Flux.just(createErrorEvent(e.getMessage()));
                });
    }

    /**
     * Stream equipment status events via SSE
     * 
     * @param equipmentType Optional filter by equipment type (VENTILATOR, HEATER, etc.)
     * @param greenhouseId Optional filter by greenhouse ID
     * @return Flux of SSE events
     */
    @GetMapping(value = "/equipment-status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamEquipmentStatus(
            @RequestParam(required = false) String equipmentType,
            @RequestParam(required = false) String greenhouseId) {
        
        log.info("New SSE connection for equipment status - equipmentType: {}, greenhouseId: {}", 
                equipmentType, greenhouseId);
        
        return sseService.getEquipmentStatusStream(equipmentType, greenhouseId)
                .map(this::toServerSentEvent)
                .onErrorResume(e -> {
                    log.error("Error in equipment status stream: {}", e.getMessage());
                    return Flux.just(createErrorEvent(e.getMessage()));
                });
    }

    /**
     * Stream all events (measurements, alerts, equipment status) via SSE
     * 
     * @param greenhouseId Optional filter by greenhouse ID
     * @return Flux of SSE events
     */
    @GetMapping(value = "/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamAll(
            @RequestParam(required = false) String greenhouseId) {
        
        log.info("New SSE connection for all events - greenhouseId: {}", greenhouseId);
        
        return sseService.getCombinedStream(greenhouseId)
                .map(this::toServerSentEvent)
                .onErrorResume(e -> {
                    log.error("Error in combined stream: {}", e.getMessage());
                    return Flux.just(createErrorEvent(e.getMessage()));
                });
    }

    /**
     * Get SSE stream statistics
     */
    @GetMapping("/stats")
    public Map<String, Object> getStreamStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("measurementClients", sseService.getMeasurementClientCount());
        stats.put("alertClients", sseService.getAlertClientCount());
        stats.put("equipmentClients", sseService.getEquipmentClientCount());
        stats.put("totalClients", sseService.getTotalClientCount());
        stats.put("timestamp", java.time.LocalDateTime.now());
        return stats;
    }

    /**
     * Health check endpoint for SSE
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "SSE Stream Service");
        health.put("totalConnections", sseService.getTotalClientCount());
        health.put("timestamp", java.time.LocalDateTime.now());
        return health;
    }

    // Helper methods

    private ServerSentEvent<String> toServerSentEvent(SSEMessage<?> message) {
        try {
            String data = objectMapper.writeValueAsString(message);
            return ServerSentEvent.<String>builder()
                    .id(message.getEventId())
                    .event(message.getEventType())
                    .data(data)
                    .retry(Duration.ofSeconds(5))
                    .build();
        } catch (Exception e) {
            log.error("Error serializing SSE message: {}", e.getMessage());
            return createErrorEvent("Serialization error");
        }
    }

    private ServerSentEvent<String> createErrorEvent(String errorMessage) {
        Map<String, String> error = new HashMap<>();
        error.put("error", errorMessage);
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        
        try {
            return ServerSentEvent.<String>builder()
                    .event("error")
                    .data(objectMapper.writeValueAsString(error))
                    .build();
        } catch (Exception e) {
            return ServerSentEvent.<String>builder()
                    .event("error")
                    .data("{\"error\": \"" + errorMessage + "\"}")
                    .build();
        }
    }
}
