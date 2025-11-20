package com.greenhouse.controle.dto;

import com.greenhouse.controle.model.ActionStatus;
import com.greenhouse.controle.model.ActionType;
import com.greenhouse.controle.model.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlActionRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Device type is required")
    private DeviceType deviceType;

    @NotNull(message = "Action type is required")
    private ActionType actionType;

    private ActionStatus status;

    private String parameters;

    private String location;

    private String executedBy;

    private String notes;
}
