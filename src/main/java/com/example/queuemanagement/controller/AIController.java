package com.example.queuemanagement.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class AIController {

    @PostMapping("/predict")
    public Map<String, String> predictSpecialist(@RequestBody Map<String, String> request) {
        
        // 1. Get the symptom text
        // Check for null to avoid crashes if input is empty
        String input = request.get("symptom");
        String symptom = (input != null) ? input.toLowerCase() : "";
        
        // 2. Set Default to "MD Specialist" (General Doctor)
        String specialization = "MD Specialist"; 

        // 3. Updated Logic based on your Hospital's Available Doctors

        // --- CARDIOLOGIST (Heart) ---
        if (symptom.contains("heart") || symptom.contains("chest") || symptom.contains("breath") || symptom.contains("pulse")) {
            specialization = "Cardiologist";
        } 
        // --- DERMATOLOGIST (Skin) ---
        else if (symptom.contains("skin") || symptom.contains("rash") || symptom.contains("itch") || symptom.contains("pimple") || symptom.contains("acne")) {
            specialization = "Dermatologist";
        } 
        // --- NEUROLOGIST (Head/Brain) ---
        else if (symptom.contains("head") || symptom.contains("dizzy") || symptom.contains("migraine") || symptom.contains("brain") || symptom.contains("nerve")) {
            specialization = "Neurologist";
        }
        // --- MD SPECIALIST (Fever, Stomach, Cold, etc.) ---
        // Since "MD Specialist" is the default, we don't strictly need this 'else if', 
        // but it helps catch specific general keywords explicitly.
        else if (symptom.contains("fever") || symptom.contains("stomach") || symptom.contains("cold") || symptom.contains("cough") || symptom.contains("vomit")) {
            specialization = "MD Specialist";
        }

        // 4. Send response
        Map<String, String> response = new HashMap<>();
        response.put("specialization", specialization);
        return response;
    }
}