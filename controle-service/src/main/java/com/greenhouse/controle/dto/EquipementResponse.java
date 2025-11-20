package com.greenhouse.controle.dto;

import com.greenhouse.controle.model.EquipementType;
import com.greenhouse.controle.model.EtatEquipement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipementResponse {

    private Long id;
    private EquipementType type;
    private String nom;
    private EtatEquipement etat;
    private LocalDateTime derniereAction;
    private Long parametreAssocie;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
