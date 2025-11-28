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
public class AlertEvent {
    private String id;
    private String type;
    private String severity;
    private String message;
    private String source;
    private String sourceId;
    private String parameterType;
    private Double currentValue;
    private Double thresholdValue;
    private String greenhouseId;
    private String greenhouseName;
    private LocalDateTime timestamp;
    private Boolean acknowledged;
}
