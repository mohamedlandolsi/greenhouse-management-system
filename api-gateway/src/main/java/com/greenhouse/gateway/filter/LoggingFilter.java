package com.greenhouse.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Generate unique request ID
        String requestId = UUID.randomUUID().toString();
        long startTime = Instant.now().toEpochMilli();
        
        // Log incoming request
        log.info("Incoming request: {} {} | Request-ID: {} | IP: {} | User-Agent: {}",
                request.getMethod(),
                request.getURI(),
                requestId,
                getClientIP(request),
                request.getHeaders().getFirst(HttpHeaders.USER_AGENT));
        
        // Add custom headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .header("X-Gateway-Time", String.valueOf(startTime))
                .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();
        
        return chain.filter(modifiedExchange).doFinally(signalType -> {
            ServerHttpResponse response = exchange.getResponse();
            long endTime = Instant.now().toEpochMilli();
            long duration = endTime - startTime;
            
            // Log response (headers may already be committed, so just log)
            log.info("Outgoing response: {} {} | Request-ID: {} | Status: {} | Duration: {}ms",
                    request.getMethod(),
                    request.getURI(),
                    requestId,
                    response.getStatusCode(),
                    duration);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String getClientIP(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }
}
