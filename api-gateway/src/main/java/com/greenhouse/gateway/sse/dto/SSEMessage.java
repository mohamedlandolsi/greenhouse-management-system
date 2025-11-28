package com.greenhouse.gateway.sse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SSEMessage<T> {
    private String eventType;
    private String eventId;
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> SSEMessage<T> of(String eventType, T data) {
        return SSEMessage.<T>builder()
                .eventType(eventType)
                .eventId(java.util.UUID.randomUUID().toString())
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static SSEMessage<String> keepAlive() {
        return SSEMessage.<String>builder()
                .eventType("keep-alive")
                .eventId(java.util.UUID.randomUUID().toString())
                .data("ping")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
