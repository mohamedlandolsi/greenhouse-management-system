package com.greenhouse.controle.repository;

import com.greenhouse.controle.model.Action;
import com.greenhouse.controle.model.StatutAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
    
    Page<Action> findByEquipementId(Long equipementId, Pageable pageable);
    
    Page<Action> findByStatut(StatutAction statut, Pageable pageable);
    
    Page<Action> findByDateExecutionBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("SELECT a FROM Action a WHERE a.equipementId = :equipementId ORDER BY a.dateExecution DESC")
    List<Action> findRecentByEquipementId(@Param("equipementId") Long equipementId, Pageable pageable);
    
    List<Action> findByParametreId(Long parametreId);
}
