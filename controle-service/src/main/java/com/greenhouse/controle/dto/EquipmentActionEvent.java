package com.greenhouse.controle.dto;

import com.greenhouse.controle.model.EquipementType;
import com.greenhouse.controle.model.StatutAction;
import com.greenhouse.controle.model.TypeAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event DTO for equipment action notifications via Kafka
 * Topic: equipment-actions
 * Consumers: Notification service (future), Dashboard (SSE)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentActionEvent {

    /**
     * Unique event identifier for idempotency
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * ID of the equipment
     */
    private Long equipementId;

    /**
     * Name of the equipment
     */
    private String equipementName;

    /**
     * Type of equipment (VENTILATEUR, CHAUFFAGE, ECLAIRAGE, ARROSAGE)
     */
    private EquipementType equipementType;

    /**
     * ID of the action
     */
    private Long actionId;

    /**
     * Type of action performed
     */
    private TypeAction typeAction;

    /**
     * Status of the action
     */
    private StatutAction statut;

    /**
     * Target value for the action
     */
    private Double valeurCible;

    /**
     * Current value before action
     */
    private Double valeurActuelle;

    /**
     * ID of the related parameter (if any)
     */
    private Long parametreId;

    /**
     * Timestamp when action was executed
     */
    private LocalDateTime dateExecution;

    /**
     * Result message
     */
    private String resultat;

    /**
     * Whether this was an automatic action triggered by alert
     */
    private Boolean isAutomatic;

    /**
     * Event creation timestamp
     */
    @Builder.Default
    private LocalDateTime eventTimestamp = LocalDateTime.now();
}
