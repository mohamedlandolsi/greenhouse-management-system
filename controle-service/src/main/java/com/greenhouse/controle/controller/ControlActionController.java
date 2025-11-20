package com.greenhouse.controle.controller;

import com.greenhouse.controle.dto.ControlActionRequest;
import com.greenhouse.controle.dto.ControlActionResponse;
import com.greenhouse.controle.model.ActionStatus;
import com.greenhouse.controle.model.DeviceType;
import com.greenhouse.controle.service.ControlActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/controle")
@RequiredArgsConstructor
@Tag(name = "Control Actions", description = "APIs for managing greenhouse control systems")
public class ControlActionController {

    private final ControlActionService service;

    @PostMapping
    @Operation(summary = "Create control action", description = "Create a new control action for a device")
    public ResponseEntity<ControlActionResponse> createAction(@Valid @RequestBody ControlActionRequest request) {
        return new ResponseEntity<>(service.createAction(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all control actions", description = "Retrieve all control actions")
    public ResponseEntity<List<ControlActionResponse>> getAllActions() {
        return ResponseEntity.ok(service.getAllActions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get control action by ID", description = "Retrieve a control action by ID")
    public ResponseEntity<ControlActionResponse> getActionById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getActionById(id));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get actions by device", description = "Retrieve control actions by device ID")
    public ResponseEntity<List<ControlActionResponse>> getActionsByDeviceId(@PathVariable String deviceId) {
        return ResponseEntity.ok(service.getActionsByDeviceId(deviceId));
    }

    @GetMapping("/device-type/{deviceType}")
    @Operation(summary = "Get actions by device type", description = "Retrieve control actions by device type")
    public ResponseEntity<List<ControlActionResponse>> getActionsByDeviceType(@PathVariable DeviceType deviceType) {
        return ResponseEntity.ok(service.getActionsByDeviceType(deviceType));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get actions by status", description = "Retrieve control actions by status")
    public ResponseEntity<List<ControlActionResponse>> getActionsByStatus(@PathVariable ActionStatus status) {
        return ResponseEntity.ok(service.getActionsByStatus(status));
    }

    @GetMapping("/location/{location}")
    @Operation(summary = "Get actions by location", description = "Retrieve control actions by location")
    public ResponseEntity<List<ControlActionResponse>> getActionsByLocation(@PathVariable String location) {
        return ResponseEntity.ok(service.getActionsByLocation(location));
    }

    @GetMapping("/timerange")
    @Operation(summary = "Get actions by time range", description = "Retrieve control actions within a time range")
    public ResponseEntity<List<ControlActionResponse>> getActionsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(service.getActionsByTimeRange(start, end));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update control action", description = "Update an existing control action")
    public ResponseEntity<ControlActionResponse> updateAction(
            @PathVariable Long id,
            @Valid @RequestBody ControlActionRequest request) {
        return ResponseEntity.ok(service.updateAction(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update action status", description = "Update the status of a control action")
    public ResponseEntity<ControlActionResponse> updateActionStatus(
            @PathVariable Long id,
            @RequestParam ActionStatus status) {
        return ResponseEntity.ok(service.updateActionStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete control action", description = "Delete a control action by ID")
    public ResponseEntity<Void> deleteAction(@PathVariable Long id) {
        service.deleteAction(id);
        return ResponseEntity.noContent().build();
    }
}
