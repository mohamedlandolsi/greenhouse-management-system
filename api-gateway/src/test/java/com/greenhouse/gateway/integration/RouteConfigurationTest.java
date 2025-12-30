package com.greenhouse.gateway.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Route Configuration Tests")
class RouteConfigurationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("should have environnement-service route configured")
    void shouldHaveEnvironnementServiceRoute() {
        // When
        var routes = routeLocator.getRoutes().collectList().block();

        // Then
        assertThat(routes).isNotNull();
        assertThat(routes.stream()
                .anyMatch(route -> route.getId().contains("environnement")))
                .isTrue();
    }

    @Test
    @DisplayName("should have controle-service route configured")
    void shouldHaveControleServiceRoute() {
        // When
        var routes = routeLocator.getRoutes().collectList().block();

        // Then
        assertThat(routes).isNotNull();
        assertThat(routes.stream()
                .anyMatch(route -> route.getId().contains("controle")))
                .isTrue();
    }

    @Test
    @DisplayName("should have correct number of routes")
    void shouldHaveCorrectNumberOfRoutes() {
        // When
        var routes = routeLocator.getRoutes().collectList().block();

        // Then
        assertThat(routes).isNotNull();
        // Expect at least 2 routes (environnement and controle)
        assertThat(routes.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("routes should have proper predicates")
    void routesShouldHaveProperPredicates() {
        // When
        var routes = routeLocator.getRoutes().collectList().block();

        // Then
        assertThat(routes).isNotNull();
        routes.forEach(route -> {
            assertThat(route.getPredicate()).isNotNull();
        });
    }
}
