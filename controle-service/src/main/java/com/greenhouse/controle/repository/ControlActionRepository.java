package com.greenhouse.controle.repository;

import com.greenhouse.controle.model.ActionStatus;
import com.greenhouse.controle.model.ControlAction;
import com.greenhouse.controle.model.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ControlActionRepository extends JpaRepository<ControlAction, Long> {

    List<ControlAction> findByDeviceId(String deviceId);

    List<ControlAction> findByDeviceType(DeviceType deviceType);

    List<ControlAction> findByStatus(ActionStatus status);

    List<ControlAction> findByLocation(String location);

    List<ControlAction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM ControlAction c WHERE c.deviceId = ?1 ORDER BY c.timestamp DESC")
    List<ControlAction> findLatestByDeviceId(String deviceId);

    @Query("SELECT c FROM ControlAction c WHERE c.status = ?1 ORDER BY c.timestamp ASC")
    List<ControlAction> findPendingActions(ActionStatus status);
}
