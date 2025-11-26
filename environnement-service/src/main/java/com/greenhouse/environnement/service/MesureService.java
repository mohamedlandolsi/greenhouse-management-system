package com.greenhouse.environnement.service;

import com.greenhouse.environnement.dto.AlertEvent;
import com.greenhouse.environnement.dto.MeasurementEvent;
import com.greenhouse.environnement.dto.MesureRequest;
import com.greenhouse.environnement.dto.MesureResponse;
import com.greenhouse.environnement.exception.ResourceNotFoundException;
import com.greenhouse.environnement.model.Mesure;
import com.greenhouse.environnement.model.Parametre;
import com.greenhouse.environnement.repository.MesureRepository;
import com.greenhouse.environnement.repository.ParametreRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MesureService {

    private final MesureRepository mesureRepository;
    private final ParametreRepository parametreRepository;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public MesureResponse createMesure(MesureRequest request) {
        log.info("Creating measurement for parameter ID: {}", request.getParametreId());

        // Verify parameter exists
        Parametre parametre = parametreRepository.findById(request.getParametreId())
                .orElseThrow(() -> new ResourceNotFoundException("Parametre", "id", request.getParametreId()));

        // Use current time if not provided
        LocalDateTime dateMesure = request.getDateMesure() != null
                ? request.getDateMesure()
                : LocalDateTime.now();

        // Check if value exceeds thresholds
        boolean isAlert = request.getValeur() < parametre.getSeuilMin()
                || request.getValeur() > parametre.getSeuilMax();

        Mesure mesure = Mesure.builder()
                .parametreId(request.getParametreId())
                .valeur(request.getValeur())
                .dateMesure(dateMesure)
                .alerte(isAlert)
                .build();

        Mesure savedMesure = mesureRepository.save(mesure);
        log.info("Measurement created with ID: {} - Alert: {}", savedMesure.getId(), isAlert);

        // Always send measurement to measurement-stream topic
        sendMeasurementToKafka(savedMesure, parametre, isAlert);

        // If alert, send to greenhouse-alerts topic
        if (isAlert) {
            sendAlertToKafka(savedMesure, parametre);
        }

        return mapToResponse(savedMesure, parametre);
    }

    public Page<MesureResponse> getAllMesures(int page, int size) {
        log.info("Fetching all measurements - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateMesure"));
        return mesureRepository.findAll(pageable)
                .map(mesure -> {
                    Parametre parametre = parametreRepository.findById(mesure.getParametreId()).orElse(null);
                    return mapToResponse(mesure, parametre);
                });
    }

    public Page<MesureResponse> getMesuresByParametreId(Long parametreId, int page, int size) {
        log.info("Fetching measurements for parameter ID: {} - page: {}, size: {}", parametreId, page, size);
        
        // Verify parameter exists
        if (!parametreRepository.existsById(parametreId)) {
            throw new ResourceNotFoundException("Parametre", "id", parametreId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateMesure"));
        return mesureRepository.findByParametreId(parametreId, pageable)
                .map(mesure -> {
                    Parametre parametre = parametreRepository.findById(mesure.getParametreId()).orElse(null);
                    return mapToResponse(mesure, parametre);
                });
    }

    public Page<MesureResponse> getMesuresByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long parametreId,
            int page,
            int size
    ) {
        log.info("Fetching measurements between {} and {} for parameter ID: {}",
                startDate, endDate, parametreId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateMesure"));

        Page<Mesure> mesures;
        if (parametreId != null) {
            // Verify parameter exists
            if (!parametreRepository.existsById(parametreId)) {
                throw new ResourceNotFoundException("Parametre", "id", parametreId);
            }
            mesures = mesureRepository.findByParametreIdAndDateMesureBetween(
                    parametreId, startDate, endDate, pageable);
        } else {
            mesures = mesureRepository.findByDateMesureBetween(startDate, endDate, pageable);
        }

        return mesures.map(mesure -> {
            Parametre parametre = parametreRepository.findById(mesure.getParametreId()).orElse(null);
            return mapToResponse(mesure, parametre);
        });
    }

    public List<MesureResponse> getRecentMesures(Long parametreId, int limit) {
        log.info("Fetching {} recent measurements for parameter ID: {}", limit, parametreId);

        // Verify parameter exists
        if (!parametreRepository.existsById(parametreId)) {
            throw new ResourceNotFoundException("Parametre", "id", parametreId);
        }

        Parametre parametre = parametreRepository.findById(parametreId).orElse(null);
        Pageable pageable = PageRequest.of(0, limit);

        return mesureRepository.findRecentByParametreId(parametreId, pageable).stream()
                .map(mesure -> mapToResponse(mesure, parametre))
                .collect(Collectors.toList());
    }

    public Page<MesureResponse> getAlerts(Long parametreId, int page, int size) {
        log.info("Fetching alerts for parameter ID: {} - page: {}, size: {}", parametreId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateMesure"));

        Page<Mesure> mesures;
        if (parametreId != null) {
            // Verify parameter exists
            if (!parametreRepository.existsById(parametreId)) {
                throw new ResourceNotFoundException("Parametre", "id", parametreId);
            }
            mesures = mesureRepository.findByParametreIdAndAlerteTrue(parametreId, pageable);
        } else {
            mesures = mesureRepository.findByAlerteTrue(pageable);
        }

        return mesures.map(mesure -> {
            Parametre parametre = parametreRepository.findById(mesure.getParametreId()).orElse(null);
            return mapToResponse(mesure, parametre);
        });
    }

    private void sendAlertToKafka(Mesure mesure, Parametre parametre) {
        String message = String.format(
                "Alert: %s value %.2f%s is outside threshold [%.2f - %.2f]",
                parametre.getType(),
                mesure.getValeur(),
                parametre.getUnite(),
                parametre.getSeuilMin(),
                parametre.getSeuilMax()
        );

        // Determine severity based on how far the value is from the threshold
        String severity = calculateSeverity(mesure.getValeur(), parametre.getSeuilMin(), parametre.getSeuilMax());

        AlertEvent alertEvent = AlertEvent.builder()
                .mesureId(mesure.getId())
                .parametreId(mesure.getParametreId())
                .parametreType(parametre.getType().name())
                .valeur(mesure.getValeur())
                .seuilMin(parametre.getSeuilMin())
                .seuilMax(parametre.getSeuilMax())
                .dateMesure(mesure.getDateMesure())
                .severity(severity)
                .message(message)
                .build();

        kafkaProducerService.sendAlert(alertEvent);
    }

    private void sendMeasurementToKafka(Mesure mesure, Parametre parametre, boolean isAlert) {
        MeasurementEvent measurementEvent = MeasurementEvent.builder()
                .mesureId(mesure.getId())
                .parametreId(mesure.getParametreId())
                .parametreType(parametre.getType().name())
                .parametreName(parametre.getNom())
                .valeur(mesure.getValeur())
                .unite(parametre.getUnite())
                .seuilMin(parametre.getSeuilMin())
                .seuilMax(parametre.getSeuilMax())
                .isAlert(isAlert)
                .dateMesure(mesure.getDateMesure())
                .build();

        kafkaProducerService.sendMeasurement(measurementEvent);
    }

    private String calculateSeverity(Double value, Double seuilMin, Double seuilMax) {
        double deviation;
        if (value < seuilMin) {
            deviation = (seuilMin - value) / seuilMin * 100;
        } else {
            deviation = (value - seuilMax) / seuilMax * 100;
        }

        if (deviation > 50) {
            return "CRITICAL";
        } else if (deviation > 25) {
            return "HIGH";
        } else if (deviation > 10) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private MesureResponse mapToResponse(Mesure mesure, Parametre parametre) {
        MesureResponse.MesureResponseBuilder builder = MesureResponse.builder()
                .id(mesure.getId())
                .parametreId(mesure.getParametreId())
                .valeur(mesure.getValeur())
                .dateMesure(mesure.getDateMesure())
                .alerte(mesure.getAlerte())
                .createdAt(mesure.getCreatedAt());

        if (parametre != null) {
            builder.parametreType(parametre.getType())
                    .seuilMin(parametre.getSeuilMin())
                    .seuilMax(parametre.getSeuilMax())
                    .unite(parametre.getUnite());
        }

        return builder.build();
    }
}
