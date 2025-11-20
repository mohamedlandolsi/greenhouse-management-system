package com.greenhouse.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestSizeLimitFilter implements GlobalFilter, Ordered {

    @Value("${gateway.request.max-size:5242880}") // Default 5MB
    private long maxRequestSize;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Long contentLength = exchange.getRequest().getHeaders().getContentLength();
        
        if (contentLength != null && contentLength > maxRequestSize) {
            log.warn("Request size {} exceeds maximum allowed size {}", contentLength, maxRequestSize);
            exchange.getResponse().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
            return exchange.getResponse().setComplete();
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
