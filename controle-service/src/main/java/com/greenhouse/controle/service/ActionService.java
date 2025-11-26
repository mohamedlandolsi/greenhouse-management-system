package com.greenhouse.controle.service;

import com.greenhouse.controle.dto.ActionRequest;
import com.greenhouse.controle.dto.ActionResponse;
import com.greenhouse.controle.dto.AlertEvent;
import com.greenhouse.controle.dto.EquipmentActionEvent;
import com.greenhouse.controle.exception.EquipementNotAvailableException;
import com.greenhouse.controle.exception.ResourceNotFoundException;
import com.greenhouse.controle.model.*;
import com.greenhouse.controle.repository.ActionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionService {

    private final ActionRepository actionRepository;
    private final EquipementService equipementService;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public ActionResponse createAction(ActionRequest request) {
        log.info("Creating new action for equipment ID: {}", request.getEquipementId());
        
        // Verify equipment exists
        Equipement equipement = equipementService.getEquipementEntityById(request.getEquipementId());
        
        Action action = new Action();
        action.setEquipementId(request.getEquipementId());
        action.setParametreId(request.getParametreId());
        action.setTypeAction(request.getTypeAction());
        action.setValeurCible(request.getValeurCible());
        action.setValeurActuelle(request.getValeurActuelle());
        action.setStatut(StatutAction.EN_ATTENTE);
        action.setResultat(request.getResultat());
        
        Action saved = actionRepository.save(action);
        
        // Execute action immediately
        executeAction(saved.getId(), equipement, false);
        
        return mapToResponse(actionRepository.findById(saved.getId()).get());
    }

    @Transactional
    public void executeAction(Long actionId, Equipement equipement, boolean isAutomatic) {
        log.info("Executing action with ID: {}", actionId);
        
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Action non trouvée avec l'ID: " + actionId));
        
        try {
            // Simulate action execution
            action.setDateExecution(LocalDateTime.now());
            action.setStatut(StatutAction.EXECUTEE);
            action.setResultat("Action exécutée avec succès");
            
            // Update equipment's last action timestamp
            equipementService.updateDerniereAction(action.getEquipementId());
            
            Action savedAction = actionRepository.save(action);
            log.info("Action executed successfully");
            
            // Publish action event to Kafka
            publishActionEvent(savedAction, equipement, isAutomatic);
            
        } catch (Exception e) {
            log.error("Failed to execute action: {}", e.getMessage());
            action.setStatut(StatutAction.ECHOUEE);
            action.setResultat("Échec de l'exécution: " + e.getMessage());
            Action savedAction = actionRepository.save(action);
            
            // Publish failed action event to Kafka
            publishActionEvent(savedAction, equipement, isAutomatic);
        }
    }

    @Transactional
    public ActionResponse createAutomaticAction(AlertEvent alert) {
        log.info("Creating automatic action for alert: {}", alert.getParametreType());
        
        EquipementType equipementType = determineEquipementType(alert);
        TypeAction typeAction = determineActionType(alert);
        
        // Find available equipment
        Optional<Equipement> equipementOpt = equipementService.findAvailableEquipementByType(equipementType);
        
        if (equipementOpt.isEmpty()) {
            log.warn("No available equipment of type {} found", equipementType);
            throw new EquipementNotAvailableException(
                "Aucun équipement disponible de type: " + equipementType);
        }
        
        Equipement equipement = equipementOpt.get();
        
        Action action = new Action();
        action.setEquipementId(equipement.getId());
        action.setParametreId(alert.getParametreId());
        action.setTypeAction(typeAction);
        action.setValeurCible(alert.getValeur() > alert.getSeuilMax() ? alert.getSeuilMax() : alert.getSeuilMin());
        action.setValeurActuelle(alert.getValeur());
        action.setStatut(StatutAction.EN_ATTENTE);
        action.setResultat("Action automatique créée suite à une alerte - " + alert.getSeverity());
        
        Action saved = actionRepository.save(action);
        log.info("Automatic action created with ID: {}", saved.getId());
        
        // Execute action (marks as automatic)
        executeAction(saved.getId(), equipement, true);
        
        return mapToResponse(actionRepository.findById(saved.getId()).get());
    }

    /**
     * Publish action event to Kafka equipment-actions topic
     */
    private void publishActionEvent(Action action, Equipement equipement, boolean isAutomatic) {
        EquipmentActionEvent event = EquipmentActionEvent.builder()
                .equipementId(action.getEquipementId())
                .equipementName(equipement.getNom())
                .equipementType(equipement.getType())
                .actionId(action.getId())
                .typeAction(action.getTypeAction())
                .statut(action.getStatut())
                .valeurCible(action.getValeurCible())
                .valeurActuelle(action.getValeurActuelle())
                .parametreId(action.getParametreId())
                .dateExecution(action.getDateExecution())
                .resultat(action.getResultat())
                .isAutomatic(isAutomatic)
                .build();
        
        kafkaProducerService.sendEquipmentAction(event);
    }

    private EquipementType determineEquipementType(AlertEvent alert) {
        String parametreType = alert.getParametreType().toLowerCase();
        
        if (parametreType.contains("temperature")) {
            // If temperature is too high, use fan; if too low, use heater
            return alert.getValeur() > alert.getSeuilMax() ? 
                EquipementType.VENTILATEUR : EquipementType.CHAUFFAGE;
        } else if (parametreType.contains("humidite") || parametreType.contains("humidity")) {
            return EquipementType.VENTILATEUR;
        } else if (parametreType.contains("luminosite") || parametreType.contains("luminosity") || parametreType.contains("light")) {
            return EquipementType.ECLAIRAGE;
        } else if (parametreType.contains("co2")) {
            return EquipementType.VENTILATEUR;
        }
        
        return EquipementType.VENTILATEUR; // default
    }

    private TypeAction determineActionType(AlertEvent alert) {
        String parametreType = alert.getParametreType().toLowerCase();
        
        if (parametreType.contains("temperature")) {
            if (alert.getValeur() > alert.getSeuilMax()) {
                return TypeAction.ACTIVER; // Activate fan
            } else if (alert.getValeur() < alert.getSeuilMin()) {
                return TypeAction.ACTIVER; // Activate heater
            }
        } else if ((parametreType.contains("humidite") || parametreType.contains("humidity")) 
                   && alert.getValeur() > alert.getSeuilMax()) {
            return TypeAction.ACTIVER; // Activate fan
        } else if ((parametreType.contains("luminosite") || parametreType.contains("luminosity") || parametreType.contains("light")) 
                   && alert.getValeur() < alert.getSeuilMin()) {
            return TypeAction.ACTIVER; // Activate light
        } else if (parametreType.contains("co2") && alert.getValeur() > alert.getSeuilMax()) {
            return TypeAction.ACTIVER; // Activate ventilation
        }
        
        return TypeAction.AJUSTER;
    }

    public Page<ActionResponse> getAllActions(int page, int size) {
        log.info("Fetching all actions - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return actionRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public ActionResponse getActionById(Long id) {
        log.info("Fetching action with ID: {}", id);
        Action action = actionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action non trouvée avec l'ID: " + id));
        return mapToResponse(action);
    }

    public Page<ActionResponse> getActionsByEquipementId(Long equipementId, int page, int size) {
        log.info("Fetching actions for equipment ID: {}", equipementId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateExecution").descending());
        return actionRepository.findByEquipementId(equipementId, pageable)
                .map(this::mapToResponse);
    }

    private ActionResponse mapToResponse(Action action) {
        ActionResponse response = new ActionResponse();
        response.setId(action.getId());
        response.setEquipementId(action.getEquipementId());
        response.setParametreId(action.getParametreId());
        response.setTypeAction(action.getTypeAction());
        response.setValeurCible(action.getValeurCible());
        response.setValeurActuelle(action.getValeurActuelle());
        response.setStatut(action.getStatut());
        response.setDateExecution(action.getDateExecution());
        response.setResultat(action.getResultat());
        response.setCreatedAt(action.getCreatedAt());
        return response;
    }
}
