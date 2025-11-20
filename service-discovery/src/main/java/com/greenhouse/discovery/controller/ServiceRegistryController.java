package com.greenhouse.discovery.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registry")
@RequiredArgsConstructor
@Slf4j
public class ServiceRegistryController {

    private final EurekaServerContext eurekaServerContext;

    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getAllServices() {
        log.info("Fetching all registered services");
        
        PeerAwareInstanceRegistry registry = eurekaServerContext.getRegistry();
        List<Application> applications = registry.getSortedApplications();
        
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> servicesList = new ArrayList<>();
        
        for (Application application : applications) {
            Map<String, Object> serviceInfo = new HashMap<>();
            serviceInfo.put("name", application.getName());
            serviceInfo.put("instanceCount", application.getInstances().size());
            
            List<Map<String, Object>> instances = application.getInstances().stream()
                    .map(this::mapInstanceInfo)
                    .collect(Collectors.toList());
            
            serviceInfo.put("instances", instances);
            servicesList.add(serviceInfo);
        }
        
        response.put("services", servicesList);
        response.put("totalServices", applications.size());
        response.put("totalInstances", applications.stream()
                .mapToInt(app -> app.getInstances().size())
                .sum());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/services/{serviceName}")
    public ResponseEntity<Map<String, Object>> getServiceByName(@PathVariable String serviceName) {
        log.info("Fetching service: {}", serviceName);
        
        PeerAwareInstanceRegistry registry = eurekaServerContext.getRegistry();
        Application application = registry.getApplication(serviceName.toUpperCase());
        
        if (application == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("name", application.getName());
        response.put("instanceCount", application.getInstances().size());
        
        List<Map<String, Object>> instances = application.getInstances().stream()
                .map(this::mapInstanceInfo)
                .collect(Collectors.toList());
        
        response.put("instances", instances);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getRegistryHealth() {
        log.info("Checking registry health");
        
        PeerAwareInstanceRegistry registry = eurekaServerContext.getRegistry();
        List<Application> applications = registry.getSortedApplications();
        
        long totalInstances = applications.stream()
                .mapToInt(app -> app.getInstances().size())
                .sum();
        
        long upInstances = applications.stream()
                .flatMap(app -> app.getInstances().stream())
                .filter(instance -> instance.getStatus() == InstanceInfo.InstanceStatus.UP)
                .count();
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", upInstances == totalInstances ? "UP" : "DEGRADED");
        health.put("totalServices", applications.size());
        health.put("totalInstances", totalInstances);
        health.put("upInstances", upInstances);
        health.put("downInstances", totalInstances - upInstances);
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }

    private Map<String, Object> mapInstanceInfo(InstanceInfo instance) {
        Map<String, Object> info = new HashMap<>();
        info.put("instanceId", instance.getInstanceId());
        info.put("hostName", instance.getHostName());
        info.put("ipAddress", instance.getIPAddr());
        info.put("port", instance.getPort());
        info.put("status", instance.getStatus().toString());
        info.put("healthCheckUrl", instance.getHealthCheckUrl());
        info.put("statusPageUrl", instance.getStatusPageUrl());
        info.put("homePageUrl", instance.getHomePageUrl());
        info.put("metadata", instance.getMetadata());
        info.put("lastUpdatedTimestamp", instance.getLastUpdatedTimestamp());
        info.put("lastDirtyTimestamp", instance.getLastDirtyTimestamp());
        return info;
    }
}
