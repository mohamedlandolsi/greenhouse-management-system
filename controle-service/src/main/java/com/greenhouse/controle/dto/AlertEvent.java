package com.greenhouse.controle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event DTO for environmental threshold violation alerts via Kafka
 * Topic: greenhouse-alerts
 * Matches the AlertEvent produced by Environnement service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {

    /**
     * Unique event identifier for idempotency
     */
    private String eventId;

    /**
     * ID of the measurement that triggered the alert
     */
    private Long mesureId;

    /**
     * ID of the associated parameter
     */
    private Long parametreId;

    /**
     * Type of parameter as string (TEMPERATURE, HUMIDITY, LUMINOSITY, CO2)
     */
    private String parametreType;

    /**
     * Measured value that triggered the alert
     */
    private Double valeur;

    /**
     * Minimum threshold
     */
    private Double seuilMin;

    /**
     * Maximum threshold
     */
    private Double seuilMax;

    /**
     * Timestamp of the measurement
     */
    private LocalDateTime dateMesure;

    /**
     * Alert severity (LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String severity;

    /**
     * Descriptive message about the alert
     */
    private String message;

    /**
     * Event creation timestamp
     */
    private LocalDateTime eventTimestamp;

    /**
     * Backward compatibility - get type from parametreType or legacy 'type' field
     */
    public String getParametreType() {
        return parametreType;
    }
}
