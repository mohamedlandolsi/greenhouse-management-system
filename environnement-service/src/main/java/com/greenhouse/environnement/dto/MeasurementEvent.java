package com.greenhouse.environnement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO for real-time measurement streaming via Kafka
 * Topic: measurement-stream
 * Consumers: Analytics service (future), Dashboard (SSE)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementEvent {

    /**
     * Unique event identifier for idempotency
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * ID of the measurement record
     */
    private Long mesureId;

    /**
     * ID of the associated parameter
     */
    private Long parametreId;

    /**
     * Type of parameter as string (TEMPERATURE, HUMIDITE, LUMINOSITE)
     */
    private String parametreType;

    /**
     * Parameter name
     */
    private String parametreName;

    /**
     * Measured value
     */
    private Double valeur;

    /**
     * Unit of measurement
     */
    private String unite;

    /**
     * Minimum threshold
     */
    private Double seuilMin;

    /**
     * Maximum threshold
     */
    private Double seuilMax;

    /**
     * Whether this measurement triggered an alert
     */
    private Boolean isAlert;

    /**
     * Timestamp of the measurement
     */
    private LocalDateTime dateMesure;

    /**
     * Event creation timestamp
     */
    @Builder.Default
    private LocalDateTime eventTimestamp = LocalDateTime.now();
}
