package com.greenhouse.controle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesureDTO {
    private Long id;
    private Long parametreId;
    private Double valeur;
    private LocalDateTime dateMesure;
    private Boolean alerte;
    private LocalDateTime createdAt;
}
