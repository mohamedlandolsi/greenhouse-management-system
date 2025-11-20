package com.greenhouse.discovery.controller;

import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        stats.put("renewsLastMin", registry.getNumOfRenewsInLastMin());
        stats.put("renewsThreshold", registry.getNumOfRenewsPerMinThreshold());
        
        // Calculate if self-preservation mode would be active
        int numOfRenewsInLastMin = registry.getNumOfRenewsInLastMin();
        int numOfRenewsPerMinThreshold = registry.getNumOfRenewsPerMinThreshold();
        boolean isSelfPreservationMode = numOfRenewsInLastMin < numOfRenewsPerMinThreshold;
        
        stats.put("selfPreservationMode", isSelfPreservationMode);
        stats.put("uptime", System.currentTimeMillis() - eurekaServerContext.getServerStartTime());
        stats.put("environment", "development");
        stats.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(stats);
    }
}
