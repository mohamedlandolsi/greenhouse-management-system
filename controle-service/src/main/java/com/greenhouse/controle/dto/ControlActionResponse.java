package com.greenhouse.controle.dto;

import com.greenhouse.controle.model.ActionStatus;
import com.greenhouse.controle.model.ActionType;
import com.greenhouse.controle.model.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlActionResponse {

    private Long id;
    private String deviceId;
    private DeviceType deviceType;
    private ActionType actionType;
    private ActionStatus status;
    private String parameters;
    private LocalDateTime timestamp;
    private LocalDateTime executedAt;
    private String executedBy;
    private String location;
    private String notes;
}
