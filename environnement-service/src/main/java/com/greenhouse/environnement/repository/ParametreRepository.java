package com.greenhouse.environnement.repository;

import com.greenhouse.environnement.model.Parametre;
import com.greenhouse.environnement.model.ParametreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametreRepository extends JpaRepository<Parametre, Long> {

    Optional<Parametre> findByType(ParametreType type);

    boolean existsByType(ParametreType type);
}
