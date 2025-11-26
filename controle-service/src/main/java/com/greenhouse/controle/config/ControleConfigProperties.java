package com.greenhouse.controle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Refreshable Configuration Properties for Controle Service
 * 
 * This class demonstrates dynamic configuration refresh capability.
 * Properties can be updated from Config Server without restarting the service.
 * 
 * To refresh: POST http://localhost:8082/actuator/refresh
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "greenhouse.controle")
@Data
public class ControleConfigProperties {

    /**
     * Automatic control configuration
     */
    private AutoControl autoControl = new AutoControl();

    /**
     * Equipment configuration
     */
    private EquipmentConfig equipment = new EquipmentConfig();

    @Data
    public static class AutoControl {
        private boolean enabled = true;
        private int delaySeconds = 5;
        private int maxRetries = 3;
        private String defaultAction = "AUTO";
    }

    @Data
    public static class EquipmentConfig {
        private int healthCheckIntervalSeconds = 300;
        private int commandTimeoutSeconds = 30;
        private boolean enableLogging = true;
    }
}
