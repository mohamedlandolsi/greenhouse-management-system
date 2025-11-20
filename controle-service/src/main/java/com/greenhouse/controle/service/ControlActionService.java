package com.greenhouse.controle.service;

import com.greenhouse.controle.dto.ControlActionRequest;
import com.greenhouse.controle.dto.ControlActionResponse;
import com.greenhouse.controle.model.ActionStatus;
import com.greenhouse.controle.model.ControlAction;
import com.greenhouse.controle.model.DeviceType;
import com.greenhouse.controle.repository.ControlActionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ControlActionService {

    private final ControlActionRepository repository;

    @Transactional
    public ControlActionResponse createAction(ControlActionRequest request) {
        log.info("Creating control action for device: {}", request.getDeviceId());
        
        ControlAction action = ControlAction.builder()
                .deviceId(request.getDeviceId())
                .deviceType(request.getDeviceType())
                .actionType(request.getActionType())
                .status(request.getStatus() != null ? request.getStatus() : ActionStatus.PENDING)
                .parameters(request.getParameters())
                .location(request.getLocation())
                .executedBy(request.getExecutedBy())
                .notes(request.getNotes())
                .timestamp(LocalDateTime.now())
                .build();

        ControlAction savedAction = repository.save(action);
        log.info("Control action created with ID: {}", savedAction.getId());
        
        return mapToResponse(savedAction);
    }

    public List<ControlActionResponse> getAllActions() {
        log.info("Fetching all control actions");
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ControlActionResponse getActionById(Long id) {
        log.info("Fetching control action by ID: {}", id);
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Control action not found with id: " + id));
    }

    public List<ControlActionResponse> getActionsByDeviceId(String deviceId) {
        log.info("Fetching control actions for device: {}", deviceId);
        return repository.findByDeviceId(deviceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ControlActionResponse> getActionsByDeviceType(DeviceType deviceType) {
        log.info("Fetching control actions for device type: {}", deviceType);
        return repository.findByDeviceType(deviceType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ControlActionResponse> getActionsByStatus(ActionStatus status) {
        log.info("Fetching control actions with status: {}", status);
        return repository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ControlActionResponse> getActionsByLocation(String location) {
        log.info("Fetching control actions for location: {}", location);
        return repository.findByLocation(location).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ControlActionResponse> getActionsByTimeRange(LocalDateTime start, LocalDateTime end) {
        log.info("Fetching control actions between {} and {}", start, end);
        return repository.findByTimestampBetween(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ControlActionResponse updateAction(Long id, ControlActionRequest request) {
        log.info("Updating control action with ID: {}", id);
        
        ControlAction existingAction = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control action not found with id: " + id));

        existingAction.setDeviceId(request.getDeviceId());
        existingAction.setDeviceType(request.getDeviceType());
        existingAction.setActionType(request.getActionType());
        existingAction.setStatus(request.getStatus());
        existingAction.setParameters(request.getParameters());
        existingAction.setLocation(request.getLocation());
        existingAction.setExecutedBy(request.getExecutedBy());
        existingAction.setNotes(request.getNotes());

        ControlAction updatedAction = repository.save(existingAction);
        log.info("Control action updated with ID: {}", updatedAction.getId());
        
        return mapToResponse(updatedAction);
    }

    @Transactional
    public ControlActionResponse updateActionStatus(Long id, ActionStatus status) {
        log.info("Updating status of control action {} to {}", id, status);
        
        ControlAction action = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control action not found with id: " + id));

        action.setStatus(status);
        if (status == ActionStatus.COMPLETED || status == ActionStatus.IN_PROGRESS) {
            action.setExecutedAt(LocalDateTime.now());
        }

        ControlAction updatedAction = repository.save(action);
        log.info("Control action status updated for ID: {}", updatedAction.getId());
        
        return mapToResponse(updatedAction);
    }

    @Transactional
    public void deleteAction(Long id) {
        log.info("Deleting control action with ID: {}", id);
        if (!repository.existsById(id)) {
            throw new RuntimeException("Control action not found with id: " + id);
        }
        repository.deleteById(id);
        log.info("Control action deleted with ID: {}", id);
    }

    private ControlActionResponse mapToResponse(ControlAction action) {
        return ControlActionResponse.builder()
                .id(action.getId())
                .deviceId(action.getDeviceId())
                .deviceType(action.getDeviceType())
                .actionType(action.getActionType())
                .status(action.getStatus())
                .parameters(action.getParameters())
                .timestamp(action.getTimestamp())
                .executedAt(action.getExecutedAt())
                .executedBy(action.getExecutedBy())
                .location(action.getLocation())
                .notes(action.getNotes())
                .build();
    }
}
