package com.example.queuemanagement.dto;

import lombok.Data;

@Data
public class DoctorRequest {
    // Login Info
    private String username;
    private String password;
    
    // Profile Info (For your new table)
    private String name;
    private String specialization;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSpecialization() {
		return specialization;
	}
	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}
}