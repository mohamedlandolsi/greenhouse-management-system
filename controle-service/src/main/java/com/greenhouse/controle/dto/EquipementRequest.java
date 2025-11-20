package com.greenhouse.controle.dto;

import com.greenhouse.controle.model.EquipementType;
import com.greenhouse.controle.model.EtatEquipement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipementRequest {

    @NotNull(message = "Le type d'équipement est obligatoire")
    private EquipementType type;

    @NotBlank(message = "Le nom de l'équipement est obligatoire")
    private String nom;

    @NotNull(message = "L'état de l'équipement est obligatoire")
    private EtatEquipement etat;

    private Long parametreAssocie;
}
