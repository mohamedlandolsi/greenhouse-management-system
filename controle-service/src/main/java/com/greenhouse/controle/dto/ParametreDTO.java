package com.greenhouse.controle.dto;

import com.greenhouse.controle.model.EquipementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametreDTO {
    private Long id;
    private String type;
    private Double seuilMin;
    private Double seuilMax;
    private String unite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
