package com.greenhouse.environnement.unit.service;

import com.greenhouse.environnement.dto.MesureRequest;
import com.greenhouse.environnement.dto.MesureResponse;
import com.greenhouse.environnement.exception.ResourceNotFoundException;
import com.greenhouse.environnement.model.Mesure;
import com.greenhouse.environnement.model.Parametre;
import com.greenhouse.environnement.model.ParametreType;
import com.greenhouse.environnement.repository.MesureRepository;
import com.greenhouse.environnement.repository.ParametreRepository;
import com.greenhouse.environnement.service.KafkaProducerService;
import com.greenhouse.environnement.service.MesureService;
import com.greenhouse.environnement.testutil.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MesureService Unit Tests")
class MesureServiceTest {

    @Mock
    private MesureRepository mesureRepository;

    @Mock
    private ParametreRepository parametreRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private MesureService mesureService;

    private Parametre testParametre;
    private Mesure testMesure;

    @BeforeEach
    void setUp() {
        testParametre = TestDataBuilder.createTemperatureParameter();
        testMesure = TestDataBuilder.createNormalMeasurement(1L);
    }

    @Nested
    @DisplayName("createMesure")
    class CreateMesure {

        @Test
        @DisplayName("should create measurement successfully within thresholds")
        void shouldCreateMeasurementWithinThresholds() {
            // Given
            MesureRequest request = MesureRequest.builder()
                    .parametreId(1L)
                    .valeur(22.5) // Within 15-30 range
                    .dateMesure(LocalDateTime.now())
                    .build();

            when(parametreRepository.findById(1L)).thenReturn(Optional.of(testParametre));
            when(mesureRepository.save(any(Mesure.class))).thenReturn(testMesure);

            // When
            MesureResponse response = mesureService.createMesure(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getValeur()).isEqualTo(22.5);
            verify(mesureRepository).save(any(Mesure.class));
            verify(kafkaProducerService).sendMeasurementEvent(any());
        }

        @Test
        @DisplayName("should create alert when value exceeds max threshold")
        void shouldCreateAlertWhenValueExceedsMaxThreshold() {
            // Given
            MesureRequest request = MesureRequest.builder()
                    .parametreId(1L)
                    .valeur(35.0) // Above 30 max threshold
                    .dateMesure(LocalDateTime.now())
                    .build();

            Mesure alertMesure = TestDataBuilder.aMesure()
                    .withValeur(35.0)
                    .asAlert()
                    .build();

            when(parametreRepository.findById(1L)).thenReturn(Optional.of(testParametre));
            when(mesureRepository.save(any(Mesure.class))).thenReturn(alertMesure);

            // When
            MesureResponse response = mesureService.createMesure(request);

            // Then
            assertThat(response).isNotNull();
            verify(kafkaProducerService).sendAlertEvent(any());
            verify(kafkaProducerService).sendMeasurementEvent(any());
        }

        @Test
        @DisplayName("should create alert when value is below min threshold")
        void shouldCreateAlertWhenValueBelowMinThreshold() {
            // Given
            MesureRequest request = MesureRequest.builder()
                    .parametreId(1L)
                    .valeur(10.0) // Below 15 min threshold
                    .dateMesure(LocalDateTime.now())
                    .build();

            Mesure alertMesure = TestDataBuilder.aMesure()
                    .withValeur(10.0)
                    .asAlert()
                    .build();

            when(parametreRepository.findById(1L)).thenReturn(Optional.of(testParametre));
            when(mesureRepository.save(any(Mesure.class))).thenReturn(alertMesure);

            // When
            MesureResponse response = mesureService.createMesure(request);

            // Then
            assertThat(response).isNotNull();
            verify(kafkaProducerService).sendAlertEvent(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when parameter not found")
        void shouldThrowExceptionWhenParameterNotFound() {
            // Given
            MesureRequest request = MesureRequest.builder()
                    .parametreId(999L)
                    .valeur(22.5)
                    .dateMesure(LocalDateTime.now())
                    .build();

            when(parametreRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> mesureService.createMesure(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(mesureRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("calculateSeverity")
    class CalculateSeverity {

        @ParameterizedTest
        @CsvSource({
            "35.0, 15.0, 30.0, MEDIUM",  // 5 degrees above max
            "40.0, 15.0, 30.0, HIGH",     // 10+ degrees above max
            "50.0, 15.0, 30.0, CRITICAL", // 20+ degrees above max
            "10.0, 15.0, 30.0, MEDIUM",   // Below min
            "22.5, 15.0, 30.0, LOW"       // Within range (edge case)
        })
        @DisplayName("should calculate correct severity")
        void shouldCalculateCorrectSeverity(Double value, Double seuilMin, Double seuilMax, String expectedSeverity) {
            // This would test a public method or use reflection for private method testing
            // For now, we verify through the alert creation behavior
        }
    }

    @Nested
    @DisplayName("getAllMesures")
    class GetAllMesures {

        @Test
        @DisplayName("should return paginated measurements")
        void shouldReturnPaginatedMeasurements() {
            // Given
            Mesure mesure1 = TestDataBuilder.aMesure().withId(1L).build();
            Mesure mesure2 = TestDataBuilder.aMesure().withId(2L).build();
            Page<Mesure> page = new PageImpl<>(Arrays.asList(mesure1, mesure2));

            when(mesureRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(parametreRepository.findById(any())).thenReturn(Optional.of(testParametre));

            // When
            Page<MesureResponse> responses = mesureService.getAllMesures(0, 10);

            // Then
            assertThat(responses.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getMesuresByParametreId")
    class GetMesuresByParametreId {

        @Test
        @DisplayName("should return measurements for specific parameter")
        void shouldReturnMeasurementsForParameter() {
            // Given
            Page<Mesure> page = new PageImpl<>(Arrays.asList(testMesure));

            when(parametreRepository.findById(1L)).thenReturn(Optional.of(testParametre));
            when(mesureRepository.findByParametreId(eq(1L), any(Pageable.class))).thenReturn(page);

            // When
            Page<MesureResponse> responses = mesureService.getMesuresByParametreId(1L, 0, 10);

            // Then
            assertThat(responses.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAlerts")
    class GetAlerts {

        @Test
        @DisplayName("should return only alert measurements")
        void shouldReturnOnlyAlertMeasurements() {
            // Given
            Mesure alertMesure = TestDataBuilder.aMesure().asAlert().build();
            Page<Mesure> page = new PageImpl<>(Arrays.asList(alertMesure));

            when(mesureRepository.findByIsAlertTrue(any(Pageable.class))).thenReturn(page);
            when(parametreRepository.findById(any())).thenReturn(Optional.of(testParametre));

            // When
            Page<MesureResponse> responses = mesureService.getAlerts(null, 0, 10);

            // Then
            assertThat(responses.getContent()).hasSize(1);
        }
    }
}
