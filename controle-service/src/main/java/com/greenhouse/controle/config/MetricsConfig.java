package com.greenhouse.controle.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for equipment control.
 * Exposes metrics to Prometheus via Micrometer.
 */
@Component
public class MetricsConfig {

    private final MeterRegistry meterRegistry;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Counter for equipment activations by type.
     */
    public Counter equipmentActivationCounter(String equipmentType) {
        return Counter.builder("greenhouse.equipment.activations.total")
                .tag("equipment_type", equipmentType)
                .description("Total number of equipment activations")
                .register(meterRegistry);
    }

    /**
     * Counter for action executions by status.
     */
    public Counter actionExecutionCounter(String status) {
        return Counter.builder("greenhouse.action.execution.total")
                .tag("status", status)
                .description("Total number of action executions")
                .register(meterRegistry);
    }

    /**
     * Increment equipment activation counter.
     */
    public void incrementEquipmentActivation(String equipmentType) {
        equipmentActivationCounter(equipmentType).increment();
    }

    /**
     * Increment action execution counter.
     */
    public void incrementActionExecution(String status) {
        actionExecutionCounter(status).increment();
    }
}
