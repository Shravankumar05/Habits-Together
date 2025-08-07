package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Find user by email address
    Optional<User> findByEmail(String email);
    
    // Find user by display name
    Optional<User> findByDisplayName(String displayName);
    
    // Check if user exists by email
    boolean existsByEmail(String email);
}
