package com.greenhouse.environnement.unit.service;

import com.greenhouse.environnement.dto.AlertEvent;
import com.greenhouse.environnement.dto.MeasurementEvent;
import com.greenhouse.environnement.service.KafkaProducerService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaProducerService Unit Tests")
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
    }

    @Nested
    @DisplayName("sendAlertEvent")
    class SendAlertEvent {

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
                    .build();

            CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
            when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

            // When
            kafkaProducerService.sendAlertEvent(alertEvent);

            // Then
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(topicCaptor.capture(), eq(alertEvent));
            assertThat(topicCaptor.getValue()).contains("alert");
        }
    }

    @Nested
    @DisplayName("sendMeasurementEvent")
    class SendMeasurementEvent {

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
                    .build();

            CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
            when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

            // When
            kafkaProducerService.sendMeasurementEvent(measurementEvent);

            // Then
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(topicCaptor.capture(), eq(measurementEvent));
            assertThat(topicCaptor.getValue()).contains("measurement");
        }
    }
}
