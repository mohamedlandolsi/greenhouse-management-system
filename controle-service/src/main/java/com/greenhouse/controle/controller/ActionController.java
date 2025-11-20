package com.greenhouse.controle.controller;

import com.greenhouse.controle.client.EnvironnementClient;
import com.greenhouse.controle.dto.*;
import com.greenhouse.controle.service.ActionService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Actions", description = "API de gestion des actions de contrôle")
public class ActionController {

    private final ActionService actionService;
    private final EnvironnementClient environnementClient;

    @PostMapping
    @Operation(summary = "Créer une action manuelle", description = "Crée une nouvelle action de contrôle manuelle")
    public ResponseEntity<ActionResponse> createAction(@Valid @RequestBody ActionRequest request) {
        ActionResponse response = actionService.createAction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les actions", description = "Récupère toutes les actions avec pagination")
    public ResponseEntity<Page<ActionResponse>> getAllActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ActionResponse> responses = actionService.getAllActions(page, size);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une action par ID", description = "Récupère les détails d'une action spécifique")
    public ResponseEntity<ActionResponse> getActionById(@PathVariable Long id) {
        ActionResponse response = actionService.getActionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/equipement/{equipementId}")
    @Operation(summary = "Obtenir les actions d'un équipement", description = "Récupère toutes les actions pour un équipement spécifique")
    public ResponseEntity<Page<ActionResponse>> getActionsByEquipementId(
            @PathVariable Long equipementId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ActionResponse> responses = actionService.getActionsByEquipementId(equipementId, page, size);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/conditions")
    @Operation(summary = "Obtenir les conditions environnementales", 
               description = "Récupère les paramètres et mesures actuelles depuis le service environnement")
    @CircuitBreaker(name = "environnement-service", fallbackMethod = "getConditionsFallback")
    public ResponseEntity<EnvironmentConditionsResponse> getCurrentConditions() {
        log.info("Fetching current environmental conditions from environnement-service");
        
        List<ParametreDTO> parametres = environnementClient.getAllParametres();
        
        EnvironmentConditionsResponse response = new EnvironmentConditionsResponse();
        response.setParametres(parametres);
        
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<EnvironmentConditionsResponse> getConditionsFallback(Exception e) {
        log.error("Circuit breaker activated for environnement-service: {}", e.getMessage());
        
        EnvironmentConditionsResponse response = new EnvironmentConditionsResponse();
        response.setParametres(Collections.emptyList());
        response.setMessage("Service environnement temporairement indisponible");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
