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
public class MeasurementEvent {
    private String id;
    private String capteurId;
    private String capteurName;
    private String parameterType;
    private Double value;
    private String unit;
    private LocalDateTime timestamp;
    private String greenhouseId;
    private String greenhouseName;
    private String zoneId;
    private String zoneName;
}
