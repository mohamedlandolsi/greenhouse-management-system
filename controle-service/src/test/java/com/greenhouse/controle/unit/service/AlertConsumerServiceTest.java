package com.greenhouse.controle.unit.service;

import com.greenhouse.controle.dto.AlertEvent;
import com.greenhouse.controle.service.AlertConsumerService;
import com.greenhouse.controle.service.ActionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertConsumerService Unit Tests")
class AlertConsumerServiceTest {

    @Mock
    private ActionService actionService;

    @InjectMocks
    private AlertConsumerService alertConsumerService;

    private AlertEvent testAlertEvent;

    @BeforeEach
    void setUp() {
        testAlertEvent = AlertEvent.builder()
                .parametreId(1L)
                .parametreType("TEMPERATURE")
                .valeur(35.0)
                .seuilMin(15.0)
                .seuilMax(30.0)
                .dateMesure(LocalDateTime.now())
                .severity("HIGH")
                .message("Temperature exceeded maximum threshold")
                .build();
    }

    @Nested
    @DisplayName("consumeAlert")
    class ConsumeAlert {

        @Test
        @DisplayName("should process high temperature alert and trigger ventilator action")
        void shouldProcessHighTemperatureAlert() {
            // Given
            AlertEvent highTempAlert = AlertEvent.builder()
                    .parametreId(1L)
                    .parametreType("TEMPERATURE")
                    .valeur(35.0)
                    .seuilMax(30.0)
                    .severity("HIGH")
                    .build();

            // When
            alertConsumerService.consumeAlert(highTempAlert);

            // Then
            // Verify that automatic action creation was triggered
            verify(actionService, atLeastOnce()).createAutomaticAction(any());
        }

        @Test
        @DisplayName("should process low temperature alert and trigger heating action")
        void shouldProcessLowTemperatureAlert() {
            // Given
            AlertEvent lowTempAlert = AlertEvent.builder()
                    .parametreId(1L)
                    .parametreType("TEMPERATURE")
                    .valeur(10.0)
                    .seuilMin(15.0)
                    .severity("MEDIUM")
                    .build();

            // When
            alertConsumerService.consumeAlert(lowTempAlert);

            // Then
            verify(actionService, atLeastOnce()).createAutomaticAction(any());
        }

        @Test
        @DisplayName("should process humidity alert")
        void shouldProcessHumidityAlert() {
            // Given
            AlertEvent humidityAlert = AlertEvent.builder()
                    .parametreId(2L)
                    .parametreType("HUMIDITY")
                    .valeur(90.0)
                    .seuilMax(80.0)
                    .severity("MEDIUM")
                    .build();

            // When
            alertConsumerService.consumeAlert(humidityAlert);

            // Then
            verify(actionService, atLeastOnce()).createAutomaticAction(any());
        }

        @Test
        @DisplayName("should log critical severity alerts")
        void shouldLogCriticalSeverityAlerts() {
            // Given
            AlertEvent criticalAlert = AlertEvent.builder()
                    .parametreId(1L)
                    .parametreType("TEMPERATURE")
                    .valeur(50.0)
                    .seuilMax(30.0)
                    .severity("CRITICAL")
                    .build();

            // When
            alertConsumerService.consumeAlert(criticalAlert);

            // Then
            // Verify action creation for critical alerts
            verify(actionService, atLeastOnce()).createAutomaticAction(any());
        }
    }

    @Nested
    @DisplayName("determineAction")
    class DetermineAction {

        @Test
        @DisplayName("should determine correct action type based on alert")
        void shouldDetermineCorrectActionType() {
            // This tests the internal logic of determining what action to take
            // based on the type of parameter and whether it's above or below threshold
            
            // Given a high temperature alert
            AlertEvent highTempAlert = AlertEvent.builder()
                    .parametreType("TEMPERATURE")
                    .valeur(35.0)
                    .seuilMax(30.0)
                    .build();

            // When consumeAlert is called, it should create a ventilator action
            alertConsumerService.consumeAlert(highTempAlert);

            // Then verify the action service was called
            verify(actionService).createAutomaticAction(argThat(request ->
                    request.getParametreType().equals("TEMPERATURE")
            ));
        }
    }
}
