package com.greenhouse.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ResponseHeaderFilter implements GlobalFilter, Ordered {

    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
            "Authorization",
            "X-Internal-Token",
            "X-API-Key",
            "Set-Cookie"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Add security headers before the response is committed
        exchange.getResponse().beforeCommit(() -> {
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();
            
            // Strip sensitive headers from downstream services
            SENSITIVE_HEADERS.forEach(headers::remove);
            
            // Add security headers
            if (!headers.containsKey("X-Content-Type-Options")) {
                headers.add("X-Content-Type-Options", "nosniff");
            }
            if (!headers.containsKey("X-Frame-Options")) {
                headers.add("X-Frame-Options", "DENY");
            }
            if (!headers.containsKey("X-XSS-Protection")) {
                headers.add("X-XSS-Protection", "1; mode=block");
            }
            if (!headers.containsKey("Strict-Transport-Security")) {
                headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            }
            
            log.debug("Added security headers to response");
            return Mono.empty();
        });
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
