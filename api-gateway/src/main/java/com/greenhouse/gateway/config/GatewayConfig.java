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
                // Environnement Service Routes with Circuit Breaker
                .route("environnement-service", r -> r
                        .path("/api/environnement/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("environnementCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/environnement"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(ipKeyResolver()))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(org.springframework.http.HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true)))
                        .uri("lb://ENVIRONNEMENT-SERVICE"))
                
                // Controle Service Routes with Circuit Breaker
                .route("controle-service", r -> r
                        .path("/api/controle/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("controleCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/controle"))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(ipKeyResolver()))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(org.springframework.http.HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true)))
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
