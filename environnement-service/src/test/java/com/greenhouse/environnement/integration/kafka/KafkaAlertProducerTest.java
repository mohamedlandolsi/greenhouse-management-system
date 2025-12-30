package com.greenhouse.environnement.integration.kafka;

import com.greenhouse.environnement.dto.AlertEvent;
import com.greenhouse.environnement.dto.MeasurementEvent;
import com.greenhouse.environnement.service.KafkaProducerService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"greenhouse-alerts", "measurement-stream"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
@DisplayName("Kafka Alert Producer Integration Tests")
class KafkaAlertProducerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    private Consumer<String, Object> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-group", "true", embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = consumerFactory.createConsumer();
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    @DisplayName("should send alert event to greenhouse-alerts topic")
    void shouldSendAlertEventToTopic() {
        // Given
        consumer.subscribe(Collections.singletonList("greenhouse-alerts"));

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
        kafkaProducerService.sendAlertEvent(alertEvent);

        // Then
        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        assertThat(records.count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("should send measurement event to measurement-stream topic")
    void shouldSendMeasurementEventToTopic() {
        // Given
        consumer.subscribe(Collections.singletonList("measurement-stream"));

        MeasurementEvent measurementEvent = MeasurementEvent.builder()
                .parametreId(1L)
                .parametreType("TEMPERATURE")
                .valeur(22.5)
                .unite("°C")
                .dateMesure(LocalDateTime.now())
                .isAlert(false)
                .build();

        // When
        kafkaProducerService.sendMeasurementEvent(measurementEvent);

        // Then
        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        assertThat(records.count()).isGreaterThan(0);
    }
}
