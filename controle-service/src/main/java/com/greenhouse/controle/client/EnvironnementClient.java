package com.greenhouse.controle.client;

import com.greenhouse.controle.dto.MesureDTO;
import com.greenhouse.controle.dto.ParametreDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "environnement-service")
public interface EnvironnementClient {
    
    @GetMapping("/api/parametres")
    List<ParametreDTO> getAllParametres();
    
    @GetMapping("/api/parametres/{id}")
    ParametreDTO getParametreById(@PathVariable("id") Long id);
    
    @GetMapping("/api/mesures/recent/{parametreId}")
    List<MesureDTO> getRecentMesures(@PathVariable("parametreId") Long parametreId);
}
