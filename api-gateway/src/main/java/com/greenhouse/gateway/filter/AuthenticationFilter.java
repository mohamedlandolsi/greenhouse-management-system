package com.greenhouse.gateway.filter;

import com.greenhouse.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * JWT Authentication Filter
 * Currently configured to allow all requests (authentication disabled by default)
 * To enable authentication:
 * 1. Set gateway.security.enabled=true in application.yml
 * 2. Configure JWT secret and expiration
 * 3. Update PUBLIC_PATHS to include paths that don't require authentication
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/actuator",
            "/api/auth",
            "/eureka"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        // For now, authentication is disabled - all requests pass through
        // TODO: Enable authentication by uncommenting the code below
        
        /*
        // Extract JWT token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        String token = authHeader.substring(7);
        Claims claims = jwtUtil.validateToken(token);
        
        if (claims == null || jwtUtil.isTokenExpired(claims)) {
            log.warn("Invalid or expired JWT token for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // Add user information to request headers for downstream services
        String username = jwtUtil.extractUsername(claims);
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-ID", username)
                .header("X-User-Roles", claims.get("roles", String.class))
                .build();
        
        log.debug("Authenticated user: {} for path: {}", username, path);
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
        */
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
