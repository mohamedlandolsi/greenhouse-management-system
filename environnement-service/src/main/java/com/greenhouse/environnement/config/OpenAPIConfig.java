package com.greenhouse.environnement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI environnementServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Environnement Service API")
                        .description("API for managing greenhouse environmental data (temperature, humidity, light, etc.)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Greenhouse Team")
                                .email("support@greenhouse.com")));
    }
}
