package com.greenhouse.environnement.controller;

import com.greenhouse.environnement.dto.ParametreRequest;
import com.greenhouse.environnement.dto.ParametreResponse;
import com.greenhouse.environnement.model.ParametreType;
import com.greenhouse.environnement.service.ParametreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parametres")
@RequiredArgsConstructor
@Tag(name = "Paramètres", description = "API de gestion des paramètres environnementaux")
public class ParametreController {

    private final ParametreService parametreService;

    @PostMapping
    @Operation(
            summary = "Créer un nouveau paramètre",
            description = "Crée un nouveau paramètre avec ses seuils minimum et maximum"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Paramètre créé avec succès",
                    content = @Content(schema = @Schema(implementation = ParametreResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Un paramètre de ce type existe déjà")
    })
    public ResponseEntity<ParametreResponse> createParametre(
            @Valid @RequestBody ParametreRequest request) {
        return new ResponseEntity<>(parametreService.createParametre(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Obtenir tous les paramètres",
            description = "Récupère la liste de tous les paramètres configurés"
    )
    @ApiResponse(responseCode = "200", description = "Liste des paramètres récupérée avec succès")
    public ResponseEntity<List<ParametreResponse>> getAllParametres() {
        return ResponseEntity.ok(parametreService.getAllParametres());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtenir un paramètre par ID",
            description = "Récupère un paramètre spécifique par son identifiant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paramètre trouvé",
                    content = @Content(schema = @Schema(implementation = ParametreResponse.class))),
            @ApiResponse(responseCode = "404", description = "Paramètre non trouvé")
    })
    public ResponseEntity<ParametreResponse> getParametreById(
            @Parameter(description = "ID du paramètre") @PathVariable Long id) {
        return ResponseEntity.ok(parametreService.getParametreById(id));
    }

    @GetMapping("/type/{type}")
    @Operation(
            summary = "Obtenir un paramètre par type",
            description = "Récupère un paramètre spécifique par son type (TEMPERATURE, HUMIDITE, LUMINOSITE)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paramètre trouvé",
                    content = @Content(schema = @Schema(implementation = ParametreResponse.class))),
            @ApiResponse(responseCode = "404", description = "Paramètre non trouvé")
    })
    public ResponseEntity<ParametreResponse> getParametreByType(
            @Parameter(description = "Type du paramètre") @PathVariable ParametreType type) {
        return ResponseEntity.ok(parametreService.getParametreByType(type));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Mettre à jour un paramètre",
            description = "Met à jour les informations d'un paramètre existant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paramètre mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = ParametreResponse.class))),
            @ApiResponse(responseCode = "404", description = "Paramètre non trouvé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<ParametreResponse> updateParametre(
            @Parameter(description = "ID du paramètre") @PathVariable Long id,
            @Valid @RequestBody ParametreRequest request) {
        return ResponseEntity.ok(parametreService.updateParametre(id, request));
    }
}
