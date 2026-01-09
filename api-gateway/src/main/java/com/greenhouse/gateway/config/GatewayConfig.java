package com.greenhouse.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Environnement Service Routes - simplified for stability
                .route("environnement-service", r -> r
                        .path("/api/environnement/**")
                        .filters(f -> f
                                .rewritePath("/api/environnement/(?<segment>.*)", "/api/${segment}"))
                        .uri("lb://ENVIRONNEMENT-SERVICE"))
                
                // Controle Service Routes - simplified for stability
                .route("controle-service", r -> r
                        .path("/api/controle/**")
                        .filters(f -> f
                                .rewritePath("/api/controle/(?<segment>.*)", "/api/${segment}"))
                        .uri("lb://CONTROLE-SERVICE"))
                
                // Service Discovery UI Routes
                .route("service-discovery", r -> r
                        .path("/eureka/web")
                        .uri("lb://SERVICE-DISCOVERY"))
                
                .route("service-discovery-static", r -> r
                        .path("/eureka/**")
                        .uri("lb://SERVICE-DISCOVERY"))
                
                .build();
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver ipKeyResolver() {
        return exchange -> reactor.core.publisher.Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown"
        );
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20, 1);
    }
}
