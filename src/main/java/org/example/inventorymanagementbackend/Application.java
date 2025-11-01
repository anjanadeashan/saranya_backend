package org.example.inventorymanagementbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application Class
 *
 * @author Inventory Management System
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("========================================");
        System.out.println("Inventory Management System Started!");
        System.out.println("API Base URL: http://localhost:8080/api");
        System.out.println("H2 Console: http://localhost:8080/h2-console");
       
        System.out.println("========================================");
    }
}