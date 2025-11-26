package com.greenhouse.environnement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Refreshable Configuration Properties for Environnement Service
 * 
 * This class demonstrates dynamic configuration refresh capability.
 * Properties in this class can be updated from Config Server without restarting the service.
 * 
 * To refresh: POST http://localhost:8081/actuator/refresh
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "greenhouse.environnement")
@Data
public class EnvironnementConfigProperties {

    /**
     * Alert threshold configuration
     */
    private AlertThresholds alertThresholds = new AlertThresholds();

    /**
     * Measurement configuration
     */
    private MeasurementConfig measurement = new MeasurementConfig();

    @Data
    public static class AlertThresholds {
        private double temperatureMax = 35.0;
        private double temperatureMin = 10.0;
        private double humidityMax = 90.0;
        private double humidityMin = 30.0;
        private double co2Max = 1500.0;
        private double lightMin = 200.0;
    }

    @Data
    public static class MeasurementConfig {
        private int retentionDays = 90;
        private int samplingIntervalSeconds = 300;
        private boolean enableAutoAlerts = true;
    }
}
