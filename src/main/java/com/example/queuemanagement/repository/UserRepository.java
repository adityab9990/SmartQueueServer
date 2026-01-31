package com.example.queuemanagement.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.queuemanagement.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    // 👇 Add this line to find all doctors
    List<User> findByRole(String role);

	long countByRole(String string);

	
}