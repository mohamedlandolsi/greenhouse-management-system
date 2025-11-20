package com.greenhouse.controle.controller;

import com.greenhouse.controle.dto.EquipementRequest;
import com.greenhouse.controle.dto.EquipementResponse;
import com.greenhouse.controle.service.EquipementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipements")
@RequiredArgsConstructor
@Tag(name = "Équipements", description = "API de gestion des équipements de serre")
public class EquipementController {

    private final EquipementService equipementService;

    @PostMapping
    @Operation(summary = "Créer un nouvel équipement", description = "Crée un nouvel équipement dans le système")
    public ResponseEntity<EquipementResponse> createEquipement(@Valid @RequestBody EquipementRequest request) {
        EquipementResponse response = equipementService.createEquipement(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lister tous les équipements", description = "Récupère la liste de tous les équipements")
    public ResponseEntity<List<EquipementResponse>> getAllEquipements() {
        List<EquipementResponse> responses = equipementService.getAllEquipements();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un équipement par ID", description = "Récupère les détails d'un équipement spécifique")
    public ResponseEntity<EquipementResponse> getEquipementById(@PathVariable Long id) {
        EquipementResponse response = equipementService.getEquipementById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un équipement", description = "Met à jour les informations d'un équipement existant")
    public ResponseEntity<EquipementResponse> updateEquipement(
            @PathVariable Long id,
            @Valid @RequestBody EquipementRequest request) {
        EquipementResponse response = equipementService.updateEquipement(id, request);
        return ResponseEntity.ok(response);
    }
}
