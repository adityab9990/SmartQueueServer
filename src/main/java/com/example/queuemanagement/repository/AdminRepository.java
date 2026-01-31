package com.example.queuemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.queuemanagement.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    // Empty is fine! You don't need custom queries here yet.
	
}