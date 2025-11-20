package com.greenhouse.environnement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesureRequest {

    @NotNull(message = "Parameter ID is required")
    private Long parametreId;

    @NotNull(message = "Value is required")
    private Double valeur;

    private LocalDateTime dateMesure;
}
