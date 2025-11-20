package com.greenhouse.controle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ControleServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ControleServiceApplication.class, args);
    }
}
