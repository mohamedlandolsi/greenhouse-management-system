package com.greenhouse.environnement.controller;

import com.greenhouse.environnement.config.EnvironnementConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Controller for Environnement Service
 * 
 * Demonstrates dynamic configuration refresh using @RefreshScope
 * 
 * To update configuration:
 * 1. Update values in Config Server (config-server/src/main/resources/config/environnement-service.yml)
 * 2. Trigger refresh: POST http://localhost:8081/actuator/refresh
 * 3. Call this endpoint to see updated values: GET http://localhost:8081/api/config/current
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final EnvironnementConfigProperties configProperties;

    /**
     * Get current configuration values
     * GET /api/config/current
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("alertThresholds", configProperties.getAlertThresholds());
        config.put("measurement", configProperties.getMeasurement());
        config.put("message", "These values are refreshable without restarting the service");
        return ResponseEntity.ok(config);
    }

    /**
     * Get refresh instructions
     * GET /api/config/refresh-info
     */
    @GetMapping("/refresh-info")
    public ResponseEntity<Map<String, Object>> getRefreshInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "environnement-service");
        info.put("refreshable", true);
        info.put("howToRefresh", Map.of(
            "step1", "Update configuration in Config Server",
            "step2", "POST http://localhost:8081/actuator/refresh",
            "step3", "GET http://localhost:8081/api/config/current to see updated values"
        ));
        info.put("refreshableProperties", new String[]{
            "greenhouse.environnement.alertThresholds.*",
            "greenhouse.environnement.measurement.*"
        });
        return ResponseEntity.ok(info);
    }
}
