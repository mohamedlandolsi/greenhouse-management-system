package com.greenhouse.discovery.controller;

import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EurekaServerContext eurekaServerContext;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        PeerAwareInstanceRegistry registry = eurekaServerContext.getRegistry();
        
        Map<String, Object> stats = new HashMap<>();
        
        // General statistics
        long renewsLastMin = registry.getNumOfRenewsInLastMin();
        int renewsThreshold = registry.getNumOfRenewsPerMinThreshold();
        
        stats.put("renewsLastMin", renewsLastMin);
        stats.put("renewsThreshold", renewsThreshold);
        
        // Calculate if self-preservation mode would be active
        boolean isSelfPreservationMode = renewsLastMin < renewsThreshold;
        
        stats.put("selfPreservationMode", isSelfPreservationMode);
        // Use JVM startup time as server start time
        long serverStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        stats.put("uptime", System.currentTimeMillis() - serverStartTime);
        stats.put("environment", "development");
        stats.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(stats);
    }
}
