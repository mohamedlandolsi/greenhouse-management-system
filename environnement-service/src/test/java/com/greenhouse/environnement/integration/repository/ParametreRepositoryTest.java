package com.greenhouse.environnement.integration.repository;

import com.greenhouse.environnement.model.Parametre;
import com.greenhouse.environnement.model.ParametreType;
import com.greenhouse.environnement.repository.ParametreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ParametreRepository Integration Tests")
class ParametreRepositoryTest {

    @Autowired
    private ParametreRepository parametreRepository;

    @Test
    @DisplayName("should save and retrieve parameter")
    void shouldSaveAndRetrieveParameter() {
        // Given
        Parametre parametre = new Parametre();
        parametre.setType(ParametreType.TEMPERATURE);
        parametre.setSeuilMin(15.0);
        parametre.setSeuilMax(30.0);
        parametre.setUnite("°C");

        // When
        Parametre saved = parametreRepository.save(parametre);
        Optional<Parametre> found = parametreRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(ParametreType.TEMPERATURE);
        assertThat(found.get().getSeuilMin()).isEqualTo(15.0);
        assertThat(found.get().getSeuilMax()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("should find parameter by type")
    void shouldFindParameterByType() {
        // Given
        Parametre parametre = new Parametre();
        parametre.setType(ParametreType.HUMIDITE);
        parametre.setSeuilMin(40.0);
        parametre.setSeuilMax(80.0);
        parametre.setUnite("%");
        parametreRepository.save(parametre);

        // When
        Optional<Parametre> found = parametreRepository.findByType(ParametreType.HUMIDITE);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(ParametreType.HUMIDITE);
    }

    @Test
    @DisplayName("should return empty when type not found")
    void shouldReturnEmptyWhenTypeNotFound() {
        // When
        Optional<Parametre> found = parametreRepository.findByType(ParametreType.TEMPERATURE);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should check if type exists")
    void shouldCheckIfTypeExists() {
        // Given
        Parametre parametre = new Parametre();
        parametre.setType(ParametreType.TEMPERATURE);
        parametre.setSeuilMin(15.0);
        parametre.setSeuilMax(30.0);
        parametre.setUnite("°C");
        parametreRepository.save(parametre);

        // When/Then
        assertThat(parametreRepository.existsByType(ParametreType.TEMPERATURE)).isTrue();
        assertThat(parametreRepository.existsByType(ParametreType.HUMIDITE)).isFalse();
    }

    @Test
    @DisplayName("should update parameter")
    void shouldUpdateParameter() {
        // Given
        Parametre parametre = new Parametre();
        parametre.setType(ParametreType.TEMPERATURE);
        parametre.setSeuilMin(15.0);
        parametre.setSeuilMax(30.0);
        parametre.setUnite("°C");
        Parametre saved = parametreRepository.save(parametre);

        // When
        saved.setSeuilMin(10.0);
        saved.setSeuilMax(35.0);
        parametreRepository.save(saved);

        // Then
        Optional<Parametre> updated = parametreRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getSeuilMin()).isEqualTo(10.0);
        assertThat(updated.get().getSeuilMax()).isEqualTo(35.0);
    }

    @Test
    @DisplayName("should delete parameter")
    void shouldDeleteParameter() {
        // Given
        Parametre parametre = new Parametre();
        parametre.setType(ParametreType.TEMPERATURE);
        parametre.setSeuilMin(15.0);
        parametre.setSeuilMax(30.0);
        parametre.setUnite("°C");
        Parametre saved = parametreRepository.save(parametre);

        // When
        parametreRepository.deleteById(saved.getId());

        // Then
        assertThat(parametreRepository.findById(saved.getId())).isEmpty();
    }
}
