package com.greenhouse.environnement.dto;

import com.greenhouse.environnement.model.ParametreType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParametreRequest {

    @NotNull(message = "Type is required")
    private ParametreType type;

    @NotNull(message = "Seuil minimum is required")
    private Double seuilMin;

    @NotNull(message = "Seuil maximum is required")
    private Double seuilMax;

    @NotNull(message = "Unit is required")
    @Size(min = 1, max = 50, message = "Unit must be between 1 and 50 characters")
    private String unite;
}
