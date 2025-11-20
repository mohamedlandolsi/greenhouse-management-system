package com.greenhouse.controle.repository;

import com.greenhouse.controle.model.Equipement;
import com.greenhouse.controle.model.EquipementType;
import com.greenhouse.controle.model.EtatEquipement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipementRepository extends JpaRepository<Equipement, Long> {
    
    List<Equipement> findByType(EquipementType type);
    
    List<Equipement> findByEtat(EtatEquipement etat);
    
    Optional<Equipement> findByTypeAndEtat(EquipementType type, EtatEquipement etat);
    
    List<Equipement> findByParametreAssocie(Long parametreAssocie);
}
