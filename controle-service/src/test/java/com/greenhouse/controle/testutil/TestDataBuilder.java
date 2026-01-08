package com.greenhouse.controle.testutil;

import com.greenhouse.controle.model.Action;
import com.greenhouse.controle.model.Equipement;
import com.greenhouse.controle.model.EquipementType;
import com.greenhouse.controle.model.EtatEquipement;
import com.greenhouse.controle.model.StatutAction;
import com.greenhouse.controle.model.TypeAction;

import java.time.LocalDateTime;

/**
 * Fluent builder for creating test data objects.
 */
public class TestDataBuilder {

    // ========== Equipement Builders ==========

    public static EquipementBuilder anEquipement() {
        return new EquipementBuilder();
    }

    public static class EquipementBuilder {
        private Long id = 1L;
        private String nom = "Ventilateur 1";
        private EquipementType type = EquipementType.VENTILATEUR;
        private EtatEquipement etat = EtatEquipement.ACTIF;
        private LocalDateTime createdAt = LocalDateTime.now();

        public EquipementBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public EquipementBuilder withNom(String nom) {
            this.nom = nom;
            return this;
        }

        public EquipementBuilder withType(EquipementType type) {
            this.type = type;
            return this;
        }

        public EquipementBuilder active() {
            this.etat = EtatEquipement.ACTIF;
            return this;
        }

        public EquipementBuilder inactive() {
            this.etat = EtatEquipement.INACTIF;
            return this;
        }

        public Equipement build() {
            Equipement equipement = new Equipement();
            equipement.setId(id);
            equipement.setNom(nom);
            equipement.setType(type);
            equipement.setEtat(etat);
            return equipement;
        }
    }

    // ========== Action Builders ==========

    public static ActionBuilder anAction() {
        return new ActionBuilder();
    }

    public static class ActionBuilder {
        private Long id = 1L;
        private Long equipementId = 1L;
        private Long parametreId = 1L;
        private TypeAction typeAction = TypeAction.ACTIVER;
        private StatutAction statut = StatutAction.EN_ATTENTE;
        private Double valeurCible = 25.0;
        private LocalDateTime dateExecution;
        private String resultat;

        public ActionBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ActionBuilder withEquipementId(Long equipementId) {
            this.equipementId = equipementId;
            return this;
        }

        public ActionBuilder withTypeAction(TypeAction typeAction) {
            this.typeAction = typeAction;
            return this;
        }

        public ActionBuilder withStatut(StatutAction statut) {
            this.statut = statut;
            return this;
        }

        public ActionBuilder executed() {
            this.statut = StatutAction.EXECUTEE;
            this.dateExecution = LocalDateTime.now();
            return this;
        }

        public ActionBuilder failed() {
            this.statut = StatutAction.ECHOUEE;
            return this;
        }

        public Action build() {
            Action action = new Action();
            action.setId(id);
            action.setEquipementId(equipementId);
            action.setParametreId(parametreId);
            action.setTypeAction(typeAction);
            action.setStatut(statut);
            action.setValeurCible(valeurCible);
            action.setDateExecution(dateExecution);
            action.setResultat(resultat);
            return action;
        }
    }

    // ========== Factory Methods ==========

    public static Equipement createVentilateur() {
        return anEquipement()
                .withType(EquipementType.VENTILATEUR)
                .withNom("Ventilateur Principal")
                .active()
                .build();
    }

    public static Equipement createChauffage() {
        return anEquipement()
                .withId(2L)
                .withType(EquipementType.CHAUFFAGE)
                .withNom("Chauffage Zone A")
                .active()
                .build();
    }

    public static Action createPendingAction(Long equipementId) {
        return anAction()
                .withEquipementId(equipementId)
                .withTypeAction(TypeAction.ACTIVER)
                .withStatut(StatutAction.EN_ATTENTE)
                .build();
    }
}
