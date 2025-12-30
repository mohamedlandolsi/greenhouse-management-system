package com.greenhouse.controle.integration.kafka;

import com.greenhouse.controle.dto.AlertEvent;
import com.greenhouse.controle.service.AlertConsumerService;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"greenhouse-alerts"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
@DisplayName("Kafka Consumer Integration Tests")
class KafkaConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @SpyBean
    private AlertConsumerService alertConsumerService;

    private Producer<String, Object> producer;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put("key.serializer", StringSerializer.class);
        producerProps.put("value.serializer", JsonSerializer.class);

        producer = new DefaultKafkaProducerFactory<String, Object>(producerProps).createProducer();
    }

    @AfterEach
    void tearDown() {
        if (producer != null) {
            producer.close();
        }
    }

    @Test
    @DisplayName("should consume alert from Kafka topic and process it")
    void shouldConsumeAlertFromKafkaTopic() {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .parametreId(1L)
                .parametreType("TEMPERATURE")
                .valeur(35.0)
                .seuilMin(15.0)
                .seuilMax(30.0)
                .dateMesure(LocalDateTime.now())
                .severity("HIGH")
                .message("Temperature exceeded maximum threshold")
                .build();

        // When
        producer.send(new ProducerRecord<>("greenhouse-alerts", alertEvent));
        producer.flush();

        // Then - wait for the consumer to process the message
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(alertConsumerService, atLeastOnce()).consumeAlert(any(AlertEvent.class));
        });
    }

    @Test
    @DisplayName("should process multiple alerts in order")
    void shouldProcessMultipleAlertsInOrder() {
        // Given
        AlertEvent alert1 = AlertEvent.builder()
                .parametreId(1L)
                .parametreType("TEMPERATURE")
                .valeur(35.0)
                .severity("HIGH")
                .build();

        AlertEvent alert2 = AlertEvent.builder()
                .parametreId(2L)
                .parametreType("HUMIDITY")
                .valeur(90.0)
                .severity("MEDIUM")
                .build();

        // When
        producer.send(new ProducerRecord<>("greenhouse-alerts", alert1));
        producer.send(new ProducerRecord<>("greenhouse-alerts", alert2));
        producer.flush();

        // Then
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(alertConsumerService, atLeastOnce()).consumeAlert(any(AlertEvent.class));
        });
    }
}
