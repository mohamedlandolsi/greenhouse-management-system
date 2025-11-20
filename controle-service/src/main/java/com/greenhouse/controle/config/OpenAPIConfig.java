package com.greenhouse.controle.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI controleServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Controle Service API")
                        .description("API for controlling greenhouse systems (irrigation, ventilation, heating, etc.)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Greenhouse Team")
                                .email("support@greenhouse.com")));
    }
}
