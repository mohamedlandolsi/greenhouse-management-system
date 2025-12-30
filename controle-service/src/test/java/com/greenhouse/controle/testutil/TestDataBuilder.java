package com.greenhouse.controle.testutil;

import com.greenhouse.controle.dto.ActionRequest;
import com.greenhouse.controle.dto.EquipementRequest;
import com.greenhouse.controle.model.Action;
import com.greenhouse.controle.model.ActionStatus;
import com.greenhouse.controle.model.ActionType;
import com.greenhouse.controle.model.Equipement;
import com.greenhouse.controle.model.EquipementType;

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
        private Boolean actif = true;
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
            this.actif = true;
            return this;
        }

        public EquipementBuilder inactive() {
            this.actif = false;
            return this;
        }

        public Equipement build() {
            Equipement equipement = new Equipement();
            equipement.setId(id);
            equipement.setNom(nom);
            equipement.setType(type);
            equipement.setActif(actif);
            return equipement;
        }

        public EquipementRequest buildRequest() {
            return EquipementRequest.builder()
                    .nom(nom)
                    .type(type)
                    .actif(actif)
                    .build();
        }
    }

    // ========== Action Builders ==========

    public static ActionBuilder anAction() {
        return new ActionBuilder();
    }

    public static class ActionBuilder {
        private Long id = 1L;
        private Long equipementId = 1L;
        private ActionType type = ActionType.ALLUMER;
        private ActionStatus status = ActionStatus.EN_ATTENTE;
        private String parametreType = "TEMPERATURE";
        private Double valeurDeclenchement = 35.0;
        private LocalDateTime dateExecution;
        private String commentaire;

        public ActionBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ActionBuilder withEquipementId(Long equipementId) {
            this.equipementId = equipementId;
            return this;
        }

        public ActionBuilder withType(ActionType type) {
            this.type = type;
            return this;
        }

        public ActionBuilder withStatus(ActionStatus status) {
            this.status = status;
            return this;
        }

        public ActionBuilder executed() {
            this.status = ActionStatus.EXECUTEE;
            this.dateExecution = LocalDateTime.now();
            return this;
        }

        public ActionBuilder failed() {
            this.status = ActionStatus.ECHOUEE;
            return this;
        }

        public Action build() {
            Action action = new Action();
            action.setId(id);
            action.setEquipementId(equipementId);
            action.setType(type);
            action.setStatus(status);
            action.setParametreType(parametreType);
            action.setValeurDeclenchement(valeurDeclenchement);
            action.setDateExecution(dateExecution);
            action.setCommentaire(commentaire);
            return action;
        }

        public ActionRequest buildRequest() {
            return ActionRequest.builder()
                    .equipementId(equipementId)
                    .type(type)
                    .parametreType(parametreType)
                    .valeurDeclenchement(valeurDeclenchement)
                    .commentaire(commentaire)
                    .build();
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
                .withType(ActionType.ALLUMER)
                .withStatus(ActionStatus.EN_ATTENTE)
                .build();
    }
}
