package com.example.queuemanagement.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     // --- CARDIOLOGIST (Heart) ---
        if (symptom.contains("heart") || symptom.contains("chest") || symptom.contains("breath") || symptom.contains("pulse") || symptom.contains("blood pressure")) {
            specialization = "Cardiologist";
        } 
        // --- DERMATOLOGIST (Skin) ---
        else if (symptom.contains("skin") || symptom.contains("rash") || symptom.contains("itch") || symptom.contains("pimple") || symptom.contains("acne") || symptom.contains("hair")) {
            specialization = "Dermatologist";
        } 
        // --- NEUROLOGIST (Head/Brain) ---
        else if (symptom.contains("head") || symptom.contains("dizzy") || symptom.contains("migraine") || symptom.contains("brain") || symptom.contains("nerve")) {
            specialization = "Neurologist";
        }
        // --- PEDIATRICIAN (Child/Baby) ---
        else if (symptom.contains("child") || symptom.contains("kid") || symptom.contains("baby") || (symptom.contains("fever") && (symptom.contains("boy") || symptom.contains("girl")))) {
            specialization = "Pediatrician";
        }
        // --- ORTHOPEDIC (Bone/Joint) ---
        else if (symptom.contains("bone") || symptom.contains("joint") || symptom.contains("knee") || symptom.contains("back") || symptom.contains("fracture")) {
            specialization = "Orthopedic";
        }
        // --- GYNECOLOGIST (Women/Pregnancy) ---
        else if (symptom.contains("period") || symptom.contains("pregnant") || symptom.contains("women") || symptom.contains("menstru")) {
            specialization = "Gynecologist";
        }
        // --- OPHTHALMOLOGIST (Eyes) ---
        else if (symptom.contains("eye") || symptom.contains("vision") || symptom.contains("blur") || symptom.contains("sight")) {
            specialization = "Ophthalmologist";
        }
        // --- ENT SPECIALIST (Ear/Nose/Throat) ---
        else if (symptom.contains("ear") || symptom.contains("nose") || symptom.contains("throat") || symptom.contains("cold") || symptom.contains("cough") || symptom.contains("sinus")) {
            specialization = "ENT Specialist";
        }
        // --- PSYCHIATRIST (Mental Health) ---
        else if (symptom.contains("sad") || symptom.contains("depress") || symptom.contains("anxiety") || symptom.contains("stress") || symptom.contains("mind")) {
            specialization = "Psychiatrist";
        }
        // --- GENERAL PHYSICIAN (Common Illness) ---
        else if (symptom.contains("fever") || symptom.contains("stomach") || symptom.contains("vomit") || symptom.contains("weak") || symptom.contains("pain")) {
            specialization = "General Physician";
        }
        // 4. Send response
        Map<String, String> response = new HashMap<>();
        response.put("specialization", specialization);
        return response;
    }
}