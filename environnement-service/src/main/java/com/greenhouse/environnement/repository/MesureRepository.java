package com.greenhouse.environnement.repository;

import com.greenhouse.environnement.model.Mesure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MesureRepository extends JpaRepository<Mesure, Long> {

    // Find all measurements with pagination
    Page<Mesure> findAll(Pageable pageable);

    // Find measurements by parameter ID with pagination
    Page<Mesure> findByParametreId(Long parametreId, Pageable pageable);

    // Find measurements by parameter ID and date range
    Page<Mesure> findByParametreIdAndDateMesureBetween(
            Long parametreId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // Find measurements by date range
    Page<Mesure> findByDateMesureBetween(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // Find all measurements that triggered alerts
    Page<Mesure> findByAlerteTrue(Pageable pageable);

    // Find alerts by parameter ID
    Page<Mesure> findByParametreIdAndAlerteTrue(Long parametreId, Pageable pageable);

    // Get recent measurements for a parameter (last N entries)
    @Query("SELECT m FROM Mesure m WHERE m.parametreId = :parametreId ORDER BY m.dateMesure DESC")
    List<Mesure> findRecentByParametreId(@Param("parametreId") Long parametreId, Pageable pageable);

    // Get latest measurement for a parameter
    @Query("SELECT m FROM Mesure m WHERE m.parametreId = :parametreId ORDER BY m.dateMesure DESC")
    List<Mesure> findLatestByParametreId(@Param("parametreId") Long parametreId);

    // Count alerts by parameter
    long countByParametreIdAndAlerteTrue(Long parametreId);

    // Count total alerts
    long countByAlerteTrue();
}
