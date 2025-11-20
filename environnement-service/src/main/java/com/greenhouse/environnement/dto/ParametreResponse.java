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
public class ParametreResponse {

    private Long id;
    private ParametreType type;
    private Double seuilMin;
    private Double seuilMax;
    private String unite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
