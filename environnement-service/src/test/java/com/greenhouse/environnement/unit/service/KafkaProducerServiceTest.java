package com.greenhouse.environnement.unit.service;

import com.greenhouse.environnement.dto.AlertEvent;
import com.greenhouse.environnement.dto.MeasurementEvent;
import com.greenhouse.environnement.service.KafkaProducerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaProducerService Unit Tests")
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, AlertEvent> alertKafkaTemplate;

    @Mock
    private KafkaTemplate<String, MeasurementEvent> measurementKafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @Nested
    @DisplayName("sendAlert")
    class SendAlert {

        @Test
        @DisplayName("should send alert event to correct topic")
        void shouldSendAlertEventToCorrectTopic() {
            // Given
            AlertEvent alertEvent = AlertEvent.builder()
                    .parametreId(1L)
                    .parametreType("TEMPERATURE")
                    .valeur(35.0)
                    .seuilMin(15.0)
                    .seuilMax(30.0)
                    .dateMesure(LocalDateTime.now())
                    .severity("HIGH")
                    .message("Temperature exceeded threshold")
                    .eventId("test-event-1")
                    .build();

            CompletableFuture<SendResult<String, AlertEvent>> future = new CompletableFuture<>();
            when(alertKafkaTemplate.send(anyString(), anyString(), any(AlertEvent.class))).thenReturn(future);

            // When
            kafkaProducerService.sendAlert(alertEvent);

            // Then
            verify(alertKafkaTemplate).send(anyString(), anyString(), eq(alertEvent));
        }
    }

    @Nested
    @DisplayName("sendMeasurement")
    class SendMeasurement {

        @Test
        @DisplayName("should send measurement event to correct topic")
        void shouldSendMeasurementEventToCorrectTopic() {
            // Given
            MeasurementEvent measurementEvent = MeasurementEvent.builder()
                    .parametreId(1L)
                    .parametreType("TEMPERATURE")
                    .valeur(22.5)
                    .unite("°C")
                    .dateMesure(LocalDateTime.now())
                    .isAlert(false)
                    .eventId("test-event-2")
                    .build();

            CompletableFuture<SendResult<String, MeasurementEvent>> future = new CompletableFuture<>();
            when(measurementKafkaTemplate.send(anyString(), anyString(), any(MeasurementEvent.class))).thenReturn(future);

            // When
            kafkaProducerService.sendMeasurement(measurementEvent);

            // Then
            verify(measurementKafkaTemplate).send(anyString(), anyString(), eq(measurementEvent));
        }
    }

    @Nested
    @DisplayName("sendAlertSync")
    class SendAlertSync {

        @Test
        @DisplayName("should send alert synchronously and return true on success")
        void shouldSendAlertSyncAndReturnTrueOnSuccess() throws Exception {
            // Given
            AlertEvent alertEvent = AlertEvent.builder()
                    .parametreId(1L)
                    .parametreType("TEMPERATURE")
                    .valeur(35.0)
                    .eventId("test-sync-event")
                    .build();

            CompletableFuture<SendResult<String, AlertEvent>> future = new CompletableFuture<>();
            SendResult<String, AlertEvent> sendResult = mock(SendResult.class);
            future.complete(sendResult);

            when(alertKafkaTemplate.send(anyString(), anyString(), any(AlertEvent.class))).thenReturn(future);

            // When
            boolean result = kafkaProducerService.sendAlertSync(alertEvent);

            // Then - the method should have been called
            verify(alertKafkaTemplate).send(anyString(), anyString(), eq(alertEvent));
        }
    }
}
