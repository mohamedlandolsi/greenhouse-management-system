package com.greenhouse.gateway.controller;

import com.greenhouse.gateway.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/environnement")
    public ResponseEntity<ErrorResponse> environnementFallback() {
        log.warn("Environnement service fallback triggered");
        
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "Environnement service is temporarily unavailable. Please try again later.",
                "/fallback/environnement",
                UUID.randomUUID().toString()
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @GetMapping("/controle")
    public ResponseEntity<ErrorResponse> controleFallback() {
        log.warn("Controle service fallback triggered");
        
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "Controle service is temporarily unavailable. Please try again later.",
                "/fallback/controle",
                UUID.randomUUID().toString()
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @GetMapping("/default")
    public ResponseEntity<ErrorResponse> defaultFallback() {
        log.warn("Default fallback triggered");
        
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "The requested service is temporarily unavailable. Please try again later.",
                "/fallback/default",
                UUID.randomUUID().toString()
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}
