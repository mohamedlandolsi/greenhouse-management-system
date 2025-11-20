package com.greenhouse.environnement.dto;

import com.greenhouse.environnement.model.ParametreType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {

    private Long mesureId;
    private Long parametreId;
    private ParametreType type;
    private Double valeur;
    private Double seuilMin;
    private Double seuilMax;
    private LocalDateTime timestamp;
    private String message;
}
