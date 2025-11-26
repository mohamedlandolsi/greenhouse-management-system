package com.greenhouse.controle.service;

import com.greenhouse.controle.dto.EquipementRequest;
import com.greenhouse.controle.dto.EquipementResponse;
import com.greenhouse.controle.exception.ResourceNotFoundException;
import com.greenhouse.controle.model.Equipement;
import com.greenhouse.controle.model.EquipementType;
import com.greenhouse.controle.model.EtatEquipement;
import com.greenhouse.controle.repository.EquipementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EquipementService {

    private final EquipementRepository equipementRepository;

    @Transactional
    public EquipementResponse createEquipement(EquipementRequest request) {
        log.info("Creating new equipment: {}", request.getNom());
        
        Equipement equipement = new Equipement();
        equipement.setType(request.getType());
        equipement.setNom(request.getNom());
        equipement.setEtat(request.getEtat());
        equipement.setParametreAssocie(request.getParametreAssocie());
        
        Equipement saved = equipementRepository.save(equipement);
        log.info("Equipment created successfully with ID: {}", saved.getId());
        
        return mapToResponse(saved);
    }

    public List<EquipementResponse> getAllEquipements() {
        log.info("Fetching all equipment");
        return equipementRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EquipementResponse getEquipementById(Long id) {
        log.info("Fetching equipment with ID: {}", id);
        Equipement equipement = equipementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement non trouvé avec l'ID: " + id));
        return mapToResponse(equipement);
    }

    /**
     * Get equipment entity by ID (for internal use when entity is needed)
     */
    public Equipement getEquipementEntityById(Long id) {
        log.info("Fetching equipment entity with ID: {}", id);
        return equipementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement non trouvé avec l'ID: " + id));
    }

    @Transactional
    public EquipementResponse updateEquipement(Long id, EquipementRequest request) {
        log.info("Updating equipment with ID: {}", id);
        
        Equipement equipement = equipementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement non trouvé avec l'ID: " + id));
        
        equipement.setType(request.getType());
        equipement.setNom(request.getNom());
        equipement.setEtat(request.getEtat());
        equipement.setParametreAssocie(request.getParametreAssocie());
        
        Equipement updated = equipementRepository.save(equipement);
        log.info("Equipment updated successfully");
        
        return mapToResponse(updated);
    }

    @Transactional
    public void updateDerniereAction(Long equipementId) {
        Equipement equipement = equipementRepository.findById(equipementId)
                .orElseThrow(() -> new ResourceNotFoundException("Équipement non trouvé avec l'ID: " + equipementId));
        equipement.setDerniereAction(LocalDateTime.now());
        equipementRepository.save(equipement);
    }

    public Optional<Equipement> findAvailableEquipementByType(EquipementType type) {
        return equipementRepository.findByTypeAndEtat(type, EtatEquipement.ACTIF);
    }

    public List<Equipement> findByParametreAssocie(Long parametreId) {
        return equipementRepository.findByParametreAssocie(parametreId);
    }

    private EquipementResponse mapToResponse(Equipement equipement) {
        EquipementResponse response = new EquipementResponse();
        response.setId(equipement.getId());
        response.setType(equipement.getType());
        response.setNom(equipement.getNom());
        response.setEtat(equipement.getEtat());
        response.setDerniereAction(equipement.getDerniereAction());
        response.setParametreAssocie(equipement.getParametreAssocie());
        response.setCreatedAt(equipement.getCreatedAt());
        response.setUpdatedAt(equipement.getUpdatedAt());
        return response;
    }
}
