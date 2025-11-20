package com.greenhouse.controle.dto;

import com.greenhouse.controle.model.TypeAction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest {

    @NotNull(message = "L'ID de l'équipement est obligatoire")
    private Long equipementId;

    private Long parametreId;

    @NotNull(message = "Le type d'action est obligatoire")
    private TypeAction typeAction;

    private Double valeurCible;

    private Double valeurActuelle;

    private String resultat;
}
