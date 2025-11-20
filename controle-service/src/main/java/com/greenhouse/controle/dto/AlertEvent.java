package com.greenhouse.controle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {
    private Long parametreId;
    private String parametreType;
    private Double valeur;
    private Double seuilMin;
    private Double seuilMax;
    private LocalDateTime dateMesure;
    private String message;
}
