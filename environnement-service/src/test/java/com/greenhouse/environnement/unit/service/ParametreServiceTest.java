package com.greenhouse.environnement.unit.service;

import com.greenhouse.environnement.dto.ParametreRequest;
import com.greenhouse.environnement.dto.ParametreResponse;
import com.greenhouse.environnement.exception.DuplicateResourceException;
import com.greenhouse.environnement.exception.ResourceNotFoundException;
import com.greenhouse.environnement.model.Parametre;
import com.greenhouse.environnement.model.ParametreType;
import com.greenhouse.environnement.repository.ParametreRepository;
import com.greenhouse.environnement.service.ParametreService;
import com.greenhouse.environnement.testutil.TestDataBuilder;
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
@DisplayName("ParametreService Unit Tests")
class ParametreServiceTest {

    @Mock
    private ParametreRepository parametreRepository;

    @InjectMocks
    private ParametreService parametreService;

    private Parametre testParametre;
    private ParametreRequest testRequest;

    @BeforeEach
    void setUp() {
        testParametre = TestDataBuilder.createTemperatureParameter();
        testRequest = TestDataBuilder.aParametre()
                .withType(ParametreType.TEMPERATURE)
                .buildRequest();
    }

    @Nested
    @DisplayName("createParametre")
    class CreateParametre {

        @Test
        @DisplayName("should create parameter successfully when type does not exist")
        void shouldCreateParameterSuccessfully() {
            // Given
            when(parametreRepository.existsByType(ParametreType.TEMPERATURE)).thenReturn(false);
            when(parametreRepository.save(any(Parametre.class))).thenReturn(testParametre);

            // When
            ParametreResponse response = parametreService.createParametre(testRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getType()).isEqualTo(ParametreType.TEMPERATURE);
            assertThat(response.getSeuilMin()).isEqualTo(15.0);
            assertThat(response.getSeuilMax()).isEqualTo(30.0);
            verify(parametreRepository).save(any(Parametre.class));
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when type already exists")
        void shouldThrowExceptionWhenTypeExists() {
            // Given
            when(parametreRepository.existsByType(ParametreType.TEMPERATURE)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> parametreService.createParametre(testRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("already exists");

            verify(parametreRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when seuilMin >= seuilMax")
        void shouldThrowExceptionWhenInvalidThresholds() {
            // Given
            ParametreRequest invalidRequest = ParametreRequest.builder()
                    .type(ParametreType.TEMPERATURE)
                    .seuilMin(30.0)
                    .seuilMax(15.0)
                    .unite("°C")
                    .build();

            when(parametreRepository.existsByType(ParametreType.TEMPERATURE)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> parametreService.createParametre(invalidRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("less than");
        }
    }

    @Nested
    @DisplayName("getAllParametres")
    class GetAllParametres {

        @Test
        @DisplayName("should return all parameters")
        void shouldReturnAllParameters() {
            // Given
            Parametre humidity = TestDataBuilder.createHumidityParameter();
            when(parametreRepository.findAll()).thenReturn(Arrays.asList(testParametre, humidity));

            // When
            List<ParametreResponse> responses = parametreService.getAllParametres();

            // Then
            assertThat(responses).hasSize(2);
            assertThat(responses).extracting(ParametreResponse::getType)
                    .containsExactlyInAnyOrder(ParametreType.TEMPERATURE, ParametreType.HUMIDITY);
        }

        @Test
        @DisplayName("should return empty list when no parameters exist")
        void shouldReturnEmptyListWhenNoParameters() {
            // Given
            when(parametreRepository.findAll()).thenReturn(List.of());

            // When
            List<ParametreResponse> responses = parametreService.getAllParametres();

            // Then
            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("getParametreById")
    class GetParametreById {

        @Test
        @DisplayName("should return parameter when found")
        void shouldReturnParameterWhenFound() {
            // Given
            when(parametreRepository.findById(1L)).thenReturn(Optional.of(testParametre));

            // When
            ParametreResponse response = parametreService.getParametreById(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getType()).isEqualTo(ParametreType.TEMPERATURE);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(parametreRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> parametreService.getParametreById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateParametre")
    class UpdateParametre {

        @Test
        @DisplayName("should update parameter successfully")
        void shouldUpdateParameterSuccessfully() {
            // Given
            ParametreRequest updateRequest = ParametreRequest.builder()
                    .type(ParametreType.TEMPERATURE)
                    .seuilMin(10.0)
                    .seuilMax(35.0)
                    .unite("°C")
                    .build();

            when(parametreRepository.findById(1L)).thenReturn(Optional.of(testParametre));
            when(parametreRepository.save(any(Parametre.class))).thenReturn(testParametre);

            // When
            ParametreResponse response = parametreService.updateParametre(1L, updateRequest);

            // Then
            assertThat(response).isNotNull();
            verify(parametreRepository).save(any(Parametre.class));
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when changing to existing type")
        void shouldThrowExceptionWhenChangingToExistingType() {
            // Given
            ParametreRequest updateRequest = ParametreRequest.builder()
                    .type(ParametreType.HUMIDITY)
                    .seuilMin(10.0)
                    .seuilMax(35.0)
                    .unite("%")
                    .build();

            when(parametreRepository.findById(1L)).thenReturn(Optional.of(testParametre));
            when(parametreRepository.existsByType(ParametreType.HUMIDITY)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> parametreService.updateParametre(1L, updateRequest))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }
}
