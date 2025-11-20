package com.greenhouse.controle.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "control_actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionStatus status;

    private String parameters;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private LocalDateTime executedAt;

    private String executedBy;

    private String location;

    private String notes;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (status == null) {
            status = ActionStatus.PENDING;
        }
    }
}
