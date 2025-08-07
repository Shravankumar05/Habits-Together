package com.example.backend.config;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== DATA INITIALIZATION ===");
        
        // Check if we have any users in the database
        long userCount = userRepository.count();
        System.out.println("Current user count: " + userCount);
        
        // Production-ready initialization - no hardcoded values
        // Users will be created through proper authentication flow
        System.out.println("DataInitializer completed - ready for production use");
    }
}
