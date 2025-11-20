package com.greenhouse.controle.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "actions", indexes = {
    @Index(name = "idx_equipement_id", columnList = "equipement_id"),
    @Index(name = "idx_parametre_id", columnList = "parametre_id"),
    @Index(name = "idx_statut", columnList = "statut"),
    @Index(name = "idx_date_execution", columnList = "date_execution")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipement_id", nullable = false)
    private Long equipementId;

    @Column(name = "parametre_id")
    private Long parametreId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_action", nullable = false)
    private TypeAction typeAction;

    @Column(name = "valeur_cible")
    private Double valeurCible;

    @Column(name = "valeur_actuelle")
    private Double valeurActuelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAction statut;

    @Column(name = "date_execution")
    private LocalDateTime dateExecution;

    @Column(columnDefinition = "TEXT")
    private String resultat;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
