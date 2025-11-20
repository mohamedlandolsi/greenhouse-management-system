package com.greenhouse.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
@Slf4j
public class HealthAggregationController {

    private final DiscoveryClient discoveryClient;
    private final WebClient.Builder webClientBuilder;

    @GetMapping("/health/aggregated")
    public Mono<ResponseEntity<Map<String, Object>>> getAggregatedHealth() {
        log.info("Fetching aggregated health status for all services");
        
        List<String> services = discoveryClient.getServices();
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("gateway", "UP");
        healthStatus.put("timestamp", System.currentTimeMillis());
        
        Map<String, String> servicesHealth = new HashMap<>();
        
        for (String serviceName : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (!instances.isEmpty()) {
                ServiceInstance instance = instances.get(0);
                servicesHealth.put(serviceName, instance.isSecure() ? "SECURE" : "UP");
            } else {
                servicesHealth.put(serviceName, "DOWN");
            }
        }
        
        healthStatus.put("services", servicesHealth);
        healthStatus.put("totalServices", services.size());
        healthStatus.put("availableServices", servicesHealth.values().stream()
                .filter(status -> !status.equals("DOWN"))
                .count());
        
        return Mono.just(ResponseEntity.ok(healthStatus));
    }

    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getRegisteredServices() {
        log.info("Fetching all registered services");
        
        List<String> services = discoveryClient.getServices();
        Map<String, Object> response = new HashMap<>();
        
        Map<String, List<Map<String, Object>>> serviceInstances = new HashMap<>();
        
        for (String serviceName : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            List<Map<String, Object>> instanceDetails = instances.stream()
                    .map(instance -> {
                        Map<String, Object> details = new HashMap<>();
                        details.put("instanceId", instance.getInstanceId());
                        details.put("host", instance.getHost());
                        details.put("port", instance.getPort());
                        details.put("uri", instance.getUri().toString());
                        details.put("secure", instance.isSecure());
                        return details;
                    })
                    .collect(Collectors.toList());
            
            serviceInstances.put(serviceName, instanceDetails);
        }
        
        response.put("services", serviceInstances);
        response.put("totalServices", services.size());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
