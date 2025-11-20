package com.greenhouse.environnement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mesures", indexes = {
    @Index(name = "idx_parametre_id", columnList = "parametreId"),
    @Index(name = "idx_date_mesure", columnList = "dateMesure"),
    @Index(name = "idx_alerte", columnList = "alerte")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mesure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long parametreId;

    @Column(nullable = false)
    private Double valeur;

    @Column(nullable = false)
    private LocalDateTime dateMesure;

    @Column(nullable = false)
    private Boolean alerte;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parametreId", insertable = false, updatable = false)
    private Parametre parametre;
}
