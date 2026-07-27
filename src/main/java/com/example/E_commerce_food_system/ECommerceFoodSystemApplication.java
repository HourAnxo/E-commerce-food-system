package com.example.E_commerce_food_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ECommerceFoodSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceFoodSystemApplication.class, args);
    }

}