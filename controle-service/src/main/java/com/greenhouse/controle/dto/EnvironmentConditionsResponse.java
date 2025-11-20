package com.greenhouse.controle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentConditionsResponse {
    private List<ParametreDTO> parametres;
    private String message;
}
