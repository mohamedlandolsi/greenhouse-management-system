package com.greenhouse.environnement.testutil;

import com.greenhouse.environnement.dto.MesureRequest;
import com.greenhouse.environnement.dto.ParametreRequest;
import com.greenhouse.environnement.model.Mesure;
import com.greenhouse.environnement.model.Parametre;
import com.greenhouse.environnement.model.ParametreType;

import java.time.LocalDateTime;

/**
 * Fluent builder for creating test data objects.
 * Provides sensible defaults that can be overridden.
 */
public class TestDataBuilder {

    // ========== Parametre Builders ==========

    public static ParametreBuilder aParametre() {
        return new ParametreBuilder();
    }

    public static class ParametreBuilder {
        private Long id = 1L;
        private ParametreType type = ParametreType.TEMPERATURE;
        private Double seuilMin = 15.0;
        private Double seuilMax = 30.0;
        private String unite = "°C";
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public ParametreBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public ParametreBuilder withType(ParametreType type) {
            this.type = type;
            return this;
        }

        public ParametreBuilder withSeuilMin(Double seuilMin) {
            this.seuilMin = seuilMin;
            return this;
        }

        public ParametreBuilder withSeuilMax(Double seuilMax) {
            this.seuilMax = seuilMax;
            return this;
        }

        public ParametreBuilder withUnite(String unite) {
            this.unite = unite;
            return this;
        }

        public Parametre build() {
            Parametre parametre = new Parametre();
            parametre.setId(id);
            parametre.setType(type);
            parametre.setSeuilMin(seuilMin);
            parametre.setSeuilMax(seuilMax);
            parametre.setUnite(unite);
            return parametre;
        }

        public ParametreRequest buildRequest() {
            return ParametreRequest.builder()
                    .type(type)
                    .seuilMin(seuilMin)
                    .seuilMax(seuilMax)
                    .unite(unite)
                    .build();
        }
    }

    // ========== Mesure Builders ==========

    public static MesureBuilder aMesure() {
        return new MesureBuilder();
    }

    public static class MesureBuilder {
        private Long id = 1L;
        private Long parametreId = 1L;
        private Double valeur = 22.5;
        private LocalDateTime dateMesure = LocalDateTime.now();
        private Boolean isAlert = false;

        public MesureBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public MesureBuilder withParametreId(Long parametreId) {
            this.parametreId = parametreId;
            return this;
        }

        public MesureBuilder withValeur(Double valeur) {
            this.valeur = valeur;
            return this;
        }

        public MesureBuilder withDateMesure(LocalDateTime dateMesure) {
            this.dateMesure = dateMesure;
            return this;
        }

        public MesureBuilder asAlert() {
            this.isAlert = true;
            return this;
        }

        public Mesure build() {
            Mesure mesure = new Mesure();
            mesure.setId(id);
            mesure.setParametreId(parametreId);
            mesure.setValeur(valeur);
            mesure.setDateMesure(dateMesure);
            mesure.setAlerte(isAlert);
            return mesure;
        }

        public MesureRequest buildRequest() {
            return MesureRequest.builder()
                    .parametreId(parametreId)
                    .valeur(valeur)
                    .dateMesure(dateMesure)
                    .build();
        }
    }

    // ========== Factory Methods for Common Scenarios ==========

    public static Parametre createTemperatureParameter() {
        return aParametre()
                .withType(ParametreType.TEMPERATURE)
                .withSeuilMin(15.0)
                .withSeuilMax(30.0)
                .withUnite("°C")
                .build();
    }

    public static Parametre createHumidityParameter() {
        return aParametre()
                .withId(2L)
                .withType(ParametreType.HUMIDITE)
                .withSeuilMin(40.0)
                .withSeuilMax(80.0)
                .withUnite("%")
                .build();
    }

    public static Mesure createNormalMeasurement(Long parametreId) {
        return aMesure()
                .withParametreId(parametreId)
                .withValeur(22.5)
                .build();
    }

    public static Mesure createAlertMeasurement(Long parametreId) {
        return aMesure()
                .withParametreId(parametreId)
                .withValeur(35.0) // Above threshold
                .asAlert()
                .build();
    }
}
