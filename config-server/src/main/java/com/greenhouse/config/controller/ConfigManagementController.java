package com.greenhouse.config.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.server.encryption.EnvironmentEncryptor;
import org.springframework.cloud.config.server.encryption.TextEncryptorLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Management Controller
 * Provides additional endpoints for configuration management and encryption testing
 */
@RestController
@RequestMapping("/config")
@Slf4j
public class ConfigManagementController {

    @Autowired(required = false)
    private TextEncryptorLocator textEncryptorLocator;

    /**
     * Check if encryption is properly configured
     * GET /config/encryption-status
     */
    @GetMapping("/encryption-status")
    public ResponseEntity<Map<String, Object>> encryptionStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("encryptionConfigured", textEncryptorLocator != null);
        status.put("message", textEncryptorLocator != null 
            ? "Encryption is properly configured. Use /encrypt and /decrypt endpoints." 
            : "Encryption not configured. Set encrypt.key property.");
        log.info("Encryption status check: {}", status.get("encryptionConfigured"));
        return ResponseEntity.ok(status);
    }

    /**
     * Get configuration locations info
     * GET /config/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> configInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Spring Cloud Config Server");
        info.put("version", "1.0.0");
        info.put("description", "Centralized configuration management for greenhouse microservices");
        info.put("endpoints", Map.of(
            "getConfig", "GET /{application}/{profile}[/{label}]",
            "encrypt", "POST /encrypt",
            "decrypt", "POST /decrypt",
            "encryptionStatus", "GET /config/encryption-status",
            "refresh", "POST /actuator/refresh (on client services)"
        ));
        info.put("profiles", new String[]{"dev", "prod", "test"});
        info.put("applications", new String[]{"environnement-service", "controle-service", "api-gateway"});
        log.info("Config info requested");
        return ResponseEntity.ok(info);
    }

    /**
     * Health check for config server
     * GET /config/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "config-server");
        return ResponseEntity.ok(health);
    }
}
