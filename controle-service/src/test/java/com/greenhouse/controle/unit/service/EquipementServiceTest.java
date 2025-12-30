package com.greenhouse.controle.unit.service;

import com.greenhouse.controle.dto.EquipementRequest;
import com.greenhouse.controle.dto.EquipementResponse;
import com.greenhouse.controle.exception.ResourceNotFoundException;
import com.greenhouse.controle.model.Equipement;
import com.greenhouse.controle.model.EquipementType;
import com.greenhouse.controle.repository.EquipementRepository;
import com.greenhouse.controle.service.EquipementService;
import com.greenhouse.controle.testutil.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipementService Unit Tests")
class EquipementServiceTest {

    @Mock
    private EquipementRepository equipementRepository;

    @InjectMocks
    private EquipementService equipementService;

    private Equipement testEquipement;

    @BeforeEach
    void setUp() {
        testEquipement = TestDataBuilder.createVentilateur();
    }

    @Nested
    @DisplayName("createEquipement")
    class CreateEquipement {

        @Test
        @DisplayName("should create equipement successfully")
        void shouldCreateEquipementSuccessfully() {
            // Given
            EquipementRequest request = TestDataBuilder.anEquipement().buildRequest();
            when(equipementRepository.save(any(Equipement.class))).thenReturn(testEquipement);

            // When
            EquipementResponse response = equipementService.createEquipement(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getNom()).isEqualTo("Ventilateur Principal");
            assertThat(response.getType()).isEqualTo(EquipementType.VENTILATEUR);
            verify(equipementRepository).save(any(Equipement.class));
        }
    }

    @Nested
    @DisplayName("getAllEquipements")
    class GetAllEquipements {

        @Test
        @DisplayName("should return all equipements")
        void shouldReturnAllEquipements() {
            // Given
            Equipement ventilateur = TestDataBuilder.createVentilateur();
            Equipement chauffage = TestDataBuilder.createChauffage();
            when(equipementRepository.findAll()).thenReturn(Arrays.asList(ventilateur, chauffage));

            // When
            List<EquipementResponse> responses = equipementService.getAllEquipements();

            // Then
            assertThat(responses).hasSize(2);
            assertThat(responses).extracting(EquipementResponse::getType)
                    .containsExactlyInAnyOrder(EquipementType.VENTILATEUR, EquipementType.CHAUFFAGE);
        }
    }

    @Nested
    @DisplayName("getEquipementById")
    class GetEquipementById {

        @Test
        @DisplayName("should return equipement when found")
        void shouldReturnEquipementWhenFound() {
            // Given
            when(equipementRepository.findById(1L)).thenReturn(Optional.of(testEquipement));

            // When
            EquipementResponse response = equipementService.getEquipementById(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw exception when not found")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(equipementRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> equipementService.getEquipementById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateEquipement")
    class UpdateEquipement {

        @Test
        @DisplayName("should update equipement successfully")
        void shouldUpdateEquipementSuccessfully() {
            // Given
            EquipementRequest updateRequest = EquipementRequest.builder()
                    .nom("Ventilateur Modifié")
                    .type(EquipementType.VENTILATEUR)
                    .actif(false)
                    .build();

            when(equipementRepository.findById(1L)).thenReturn(Optional.of(testEquipement));
            when(equipementRepository.save(any(Equipement.class))).thenReturn(testEquipement);

            // When
            EquipementResponse response = equipementService.updateEquipement(1L, updateRequest);

            // Then
            assertThat(response).isNotNull();
            verify(equipementRepository).save(any(Equipement.class));
        }
    }

    @Nested
    @DisplayName("getEquipementsByType")
    class GetEquipementsByType {

        @Test
        @DisplayName("should return equipements filtered by type")
        void shouldReturnEquipementsFilteredByType() {
            // Given
            when(equipementRepository.findByType(EquipementType.VENTILATEUR))
                    .thenReturn(Arrays.asList(testEquipement));

            // When
            List<EquipementResponse> responses = equipementService.getEquipementsByType(
                    EquipementType.VENTILATEUR
            );

            // Then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getType()).isEqualTo(EquipementType.VENTILATEUR);
        }
    }

    @Nested
    @DisplayName("toggleEquipementStatus")
    class ToggleEquipementStatus {

        @Test
        @DisplayName("should toggle active status from true to false")
        void shouldToggleActiveStatusFromTrueToFalse() {
            // Given
            Equipement activeEquipement = TestDataBuilder.anEquipement().active().build();
            when(equipementRepository.findById(1L)).thenReturn(Optional.of(activeEquipement));
            when(equipementRepository.save(any(Equipement.class))).thenAnswer(inv -> {
                Equipement e = inv.getArgument(0);
                return e;
            });

            // When
            EquipementResponse response = equipementService.toggleEquipementStatus(1L);

            // Then
            assertThat(response.getActif()).isFalse();
        }

        @Test
        @DisplayName("should toggle active status from false to true")
        void shouldToggleActiveStatusFromFalseToTrue() {
            // Given
            Equipement inactiveEquipement = TestDataBuilder.anEquipement().inactive().build();
            when(equipementRepository.findById(1L)).thenReturn(Optional.of(inactiveEquipement));
            when(equipementRepository.save(any(Equipement.class))).thenAnswer(inv -> {
                Equipement e = inv.getArgument(0);
                return e;
            });

            // When
            EquipementResponse response = equipementService.toggleEquipementStatus(1L);

            // Then
            assertThat(response.getActif()).isTrue();
        }
    }
}
