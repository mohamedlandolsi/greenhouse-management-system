package com.greenhouse.environnement.service;

import com.greenhouse.environnement.dto.ParametreRequest;
import com.greenhouse.environnement.dto.ParametreResponse;
import com.greenhouse.environnement.exception.DuplicateResourceException;
import com.greenhouse.environnement.exception.ResourceNotFoundException;
import com.greenhouse.environnement.model.Parametre;
import com.greenhouse.environnement.model.ParametreType;
import com.greenhouse.environnement.repository.ParametreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParametreService {

    private final ParametreRepository parametreRepository;

    @Transactional
    public ParametreResponse createParametre(ParametreRequest request) {
        log.info("Creating parameter of type: {}", request.getType());

        // Check if parameter type already exists
        if (parametreRepository.existsByType(request.getType())) {
            throw new DuplicateResourceException(
                    "Parameter with type " + request.getType() + " already exists"
            );
        }

        // Validate thresholds
        if (request.getSeuilMin() >= request.getSeuilMax()) {
            throw new IllegalArgumentException("Seuil minimum must be less than seuil maximum");
        }

        Parametre parametre = Parametre.builder()
                .type(request.getType())
                .seuilMin(request.getSeuilMin())
                .seuilMax(request.getSeuilMax())
                .unite(request.getUnite())
                .build();

        Parametre savedParametre = parametreRepository.save(parametre);
        log.info("Parameter created with ID: {}", savedParametre.getId());

        return mapToResponse(savedParametre);
    }

    public List<ParametreResponse> getAllParametres() {
        log.info("Fetching all parameters");
        return parametreRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ParametreResponse getParametreById(Long id) {
        log.info("Fetching parameter by ID: {}", id);
        Parametre parametre = parametreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parametre", "id", id));
        return mapToResponse(parametre);
    }

    public ParametreResponse getParametreByType(ParametreType type) {
        log.info("Fetching parameter by type: {}", type);
        Parametre parametre = parametreRepository.findByType(type)
                .orElseThrow(() -> new ResourceNotFoundException("Parametre", "type", type));
        return mapToResponse(parametre);
    }

    @Transactional
    public ParametreResponse updateParametre(Long id, ParametreRequest request) {
        log.info("Updating parameter with ID: {}", id);

        Parametre existingParametre = parametreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parametre", "id", id));

        // If type is being changed, check if new type already exists
        if (!existingParametre.getType().equals(request.getType())) {
            if (parametreRepository.existsByType(request.getType())) {
                throw new DuplicateResourceException(
                        "Parameter with type " + request.getType() + " already exists"
                );
            }
            existingParametre.setType(request.getType());
        }

        // Validate thresholds
        if (request.getSeuilMin() >= request.getSeuilMax()) {
            throw new IllegalArgumentException("Seuil minimum must be less than seuil maximum");
        }

        existingParametre.setSeuilMin(request.getSeuilMin());
        existingParametre.setSeuilMax(request.getSeuilMax());
        existingParametre.setUnite(request.getUnite());

        Parametre updatedParametre = parametreRepository.save(existingParametre);
        log.info("Parameter updated with ID: {}", updatedParametre.getId());

        return mapToResponse(updatedParametre);
    }

    private ParametreResponse mapToResponse(Parametre parametre) {
        return ParametreResponse.builder()
                .id(parametre.getId())
                .type(parametre.getType())
                .seuilMin(parametre.getSeuilMin())
                .seuilMax(parametre.getSeuilMax())
                .unite(parametre.getUnite())
                .createdAt(parametre.getCreatedAt())
                .updatedAt(parametre.getUpdatedAt())
                .build();
    }
}
