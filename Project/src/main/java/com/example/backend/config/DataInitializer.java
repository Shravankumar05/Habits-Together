package com.example.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // Application startup initialization
        // No hardcoded user creation for security
        System.out.println("Habit Tracker application initialized successfully");
    }
}
