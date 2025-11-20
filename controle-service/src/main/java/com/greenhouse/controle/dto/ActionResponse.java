package com.greenhouse.controle.dto;

import com.greenhouse.controle.model.StatutAction;
import com.greenhouse.controle.model.TypeAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionResponse {

    private Long id;
    private Long equipementId;
    private Long parametreId;
    private TypeAction typeAction;
    private Double valeurCible;
    private Double valeurActuelle;
    private StatutAction statut;
    private LocalDateTime dateExecution;
    private String resultat;
    private LocalDateTime createdAt;
}
