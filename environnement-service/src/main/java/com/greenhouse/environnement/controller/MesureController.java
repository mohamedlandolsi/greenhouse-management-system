package com.greenhouse.environnement.controller;

import com.greenhouse.environnement.dto.MesureRequest;
import com.greenhouse.environnement.dto.MesureResponse;
import com.greenhouse.environnement.service.MesureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mesures")
@RequiredArgsConstructor
@Tag(name = "Mesures", description = "API de gestion des mesures environnementales")
public class MesureController {

    private final MesureService mesureService;

    @PostMapping
    @Operation(
            summary = "Enregistrer une nouvelle mesure",
            description = "Enregistre une nouvelle mesure et vérifie automatiquement si elle dépasse les seuils"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mesure enregistrée avec succès",
                    content = @Content(schema = @Schema(implementation = MesureResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Paramètre non trouvé")
    })
    public ResponseEntity<MesureResponse> createMesure(
            @Valid @RequestBody MesureRequest request) {
        return new ResponseEntity<>(mesureService.createMesure(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Obtenir toutes les mesures avec pagination",
            description = "Récupère la liste paginée de toutes les mesures"
    )
    @ApiResponse(responseCode = "200", description = "Liste des mesures récupérée avec succès")
    public ResponseEntity<Page<MesureResponse>> getAllMesures(
            @Parameter(description = "Numéro de page (0-indexé)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mesureService.getAllMesures(page, size));
    }

    @GetMapping("/parametre/{parametreId}")
    @Operation(
            summary = "Obtenir les mesures d'un paramètre",
            description = "Récupère toutes les mesures pour un paramètre spécifique avec pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesures récupérées avec succès"),
            @ApiResponse(responseCode = "404", description = "Paramètre non trouvé")
    })
    public ResponseEntity<Page<MesureResponse>> getMesuresByParametre(
            @Parameter(description = "ID du paramètre") @PathVariable Long parametreId,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mesureService.getMesuresByParametreId(parametreId, page, size));
    }

    @GetMapping("/recent/{parametreId}")
    @Operation(
            summary = "Obtenir les mesures récentes d'un paramètre",
            description = "Récupère les N dernières mesures pour un paramètre spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesures récentes récupérées avec succès"),
            @ApiResponse(responseCode = "404", description = "Paramètre non trouvé")
    })
    public ResponseEntity<List<MesureResponse>> getRecentMesures(
            @Parameter(description = "ID du paramètre") @PathVariable Long parametreId,
            @Parameter(description = "Nombre de mesures à récupérer") @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(mesureService.getRecentMesures(parametreId, limit));
    }

    @GetMapping("/filter")
    @Operation(
            summary = "Filtrer les mesures par période",
            description = "Récupère les mesures entre deux dates, optionnellement filtrées par paramètre"
    )
    @ApiResponse(responseCode = "200", description = "Mesures filtrées récupérées avec succès")
    public ResponseEntity<Page<MesureResponse>> getMesuresByDateRange(
            @Parameter(description = "Date de début (format ISO: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Date de fin (format ISO: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "ID du paramètre (optionnel)")
            @RequestParam(required = false) Long parametreId,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mesureService.getMesuresByDateRange(startDate, endDate, parametreId, page, size));
    }

    @GetMapping("/alerts")
    @Operation(
            summary = "Obtenir les mesures avec alertes",
            description = "Récupère toutes les mesures qui ont déclenché une alerte (dépassement de seuils)"
    )
    @ApiResponse(responseCode = "200", description = "Alertes récupérées avec succès")
    public ResponseEntity<Page<MesureResponse>> getAlerts(
            @Parameter(description = "ID du paramètre (optionnel)")
            @RequestParam(required = false) Long parametreId,
            @Parameter(description = "Numéro de page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mesureService.getAlerts(parametreId, page, size));
    }
}
