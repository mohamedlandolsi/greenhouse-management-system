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
public class EquipmentStatusEvent {
    private String id;
    private String equipmentId;
    private String equipmentName;
    private String equipmentType;
    private String status;
    private String previousStatus;
    private String greenhouseId;
    private String greenhouseName;
    private String zoneId;
    private String zoneName;
    private LocalDateTime timestamp;
    private String triggeredBy;
}
