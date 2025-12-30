package com.greenhouse.environnement.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenhouse.environnement.dto.ParametreRequest;
import com.greenhouse.environnement.model.Parametre;
import com.greenhouse.environnement.model.ParametreType;
import com.greenhouse.environnement.repository.ParametreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ParametreController Integration Tests")
class ParametreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParametreRepository parametreRepository;

    @BeforeEach
    void setUp() {
        parametreRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/environnement/parametres")
    class CreateParametre {

        @Test
        @DisplayName("should create parameter and return 201")
        void shouldCreateParameterSuccessfully() throws Exception {
            // Given
            ParametreRequest request = ParametreRequest.builder()
                    .type(ParametreType.TEMPERATURE)
                    .seuilMin(15.0)
                    .seuilMax(30.0)
                    .unite("°C")
                    .build();

            // When
            ResultActions result = mockMvc.perform(post("/api/environnement/parametres")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then
            result.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.type").value("TEMPERATURE"))
                    .andExpect(jsonPath("$.seuilMin").value(15.0))
                    .andExpect(jsonPath("$.seuilMax").value(30.0))
                    .andExpect(jsonPath("$.unite").value("°C"));
        }

        @Test
        @DisplayName("should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            // Given - invalid request with null type
            String invalidRequest = """
                {
                    "seuilMin": 15.0,
                    "seuilMax": 30.0,
                    "unite": "°C"
                }
                """;

            // When
            ResultActions result = mockMvc.perform(post("/api/environnement/parametres")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest));

            // Then
            result.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 409 when parameter type already exists")
        void shouldReturn409WhenDuplicateType() throws Exception {
            // Given - create existing parameter
            Parametre existing = new Parametre();
            existing.setType(ParametreType.TEMPERATURE);
            existing.setSeuilMin(15.0);
            existing.setSeuilMax(30.0);
            existing.setUnite("°C");
            parametreRepository.save(existing);

            ParametreRequest request = ParametreRequest.builder()
                    .type(ParametreType.TEMPERATURE)
                    .seuilMin(10.0)
                    .seuilMax(25.0)
                    .unite("°C")
                    .build();

            // When
            ResultActions result = mockMvc.perform(post("/api/environnement/parametres")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then
            result.andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/environnement/parametres")
    class GetAllParametres {

        @Test
        @DisplayName("should return all parameters")
        void shouldReturnAllParameters() throws Exception {
            // Given
            createParameter(ParametreType.TEMPERATURE, 15.0, 30.0, "°C");
            createParameter(ParametreType.HUMIDITY, 40.0, 80.0, "%");

            // When
            ResultActions result = mockMvc.perform(get("/api/environnement/parametres"));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].type", containsInAnyOrder("TEMPERATURE", "HUMIDITY")));
        }

        @Test
        @DisplayName("should return empty list when no parameters")
        void shouldReturnEmptyList() throws Exception {
            // When
            ResultActions result = mockMvc.perform(get("/api/environnement/parametres"));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/environnement/parametres/{id}")
    class GetParametreById {

        @Test
        @DisplayName("should return parameter when found")
        void shouldReturnParameterWhenFound() throws Exception {
            // Given
            Parametre parametre = createParameter(ParametreType.TEMPERATURE, 15.0, 30.0, "°C");

            // When
            ResultActions result = mockMvc.perform(get("/api/environnement/parametres/{id}", parametre.getId()));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(parametre.getId()))
                    .andExpect(jsonPath("$.type").value("TEMPERATURE"));
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // When
            ResultActions result = mockMvc.perform(get("/api/environnement/parametres/{id}", 999L));

            // Then
            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/environnement/parametres/{id}")
    class UpdateParametre {

        @Test
        @DisplayName("should update parameter successfully")
        void shouldUpdateParameterSuccessfully() throws Exception {
            // Given
            Parametre parametre = createParameter(ParametreType.TEMPERATURE, 15.0, 30.0, "°C");

            ParametreRequest updateRequest = ParametreRequest.builder()
                    .type(ParametreType.TEMPERATURE)
                    .seuilMin(10.0)
                    .seuilMax(35.0)
                    .unite("°C")
                    .build();

            // When
            ResultActions result = mockMvc.perform(put("/api/environnement/parametres/{id}", parametre.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)));

            // Then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.seuilMin").value(10.0))
                    .andExpect(jsonPath("$.seuilMax").value(35.0));
        }
    }

    // Helper method
    private Parametre createParameter(ParametreType type, Double seuilMin, Double seuilMax, String unite) {
        Parametre parametre = new Parametre();
        parametre.setType(type);
        parametre.setSeuilMin(seuilMin);
        parametre.setSeuilMax(seuilMax);
        parametre.setUnite(unite);
        return parametreRepository.save(parametre);
    }
}
