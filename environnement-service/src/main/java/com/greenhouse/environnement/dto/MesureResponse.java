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
public class MesureResponse {

    private Long id;
    private Long parametreId;
    private ParametreType parametreType;
    private Double valeur;
    private LocalDateTime dateMesure;
    private Boolean alerte;
    private LocalDateTime createdAt;
    private Double seuilMin;
    private Double seuilMax;
    private String unite;
}
