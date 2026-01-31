package com.example.queuemanagement.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.queuemanagement.dto.DoctorRequest;
import com.example.queuemanagement.model.Doctor;
import com.example.queuemanagement.model.User;
import com.example.queuemanagement.repository.AppointmentRepository;
import com.example.queuemanagement.repository.DoctorRepository;
import com.example.queuemanagement.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000") // ✅ Allow Frontend Access
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // --- 1. GET ALL USERS (For Patient List) ---
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // --- 2. ADD DOCTOR ---
    @PostMapping("/add-doctor")
    public ResponseEntity<?> addDoctor(@RequestBody DoctorRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        // A. Create Login User
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword());
        newUser.setRole("DOCTOR");
        newUser.setVerified(true); 
        User savedUser = userRepository.save(newUser);

        // B. Create Doctor Profile
        Doctor newDoctor = new Doctor();
        newDoctor.setName(request.getName());
        newDoctor.setSpecialization(request.getSpecialization());
        newDoctor.setDoctorId(savedUser.getId()); 
        
        doctorRepository.save(newDoctor);

        return ResponseEntity.ok("Doctor registered successfully!");
    }

    // --- 3. UPDATE DOCTOR ---
    @PutMapping("/doctor/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody DoctorRequest request) {
        // A. Update User Login Info (Password)
        // Note: 'id' here is the Doctor Table ID
        Optional<Doctor> docOpt = doctorRepository.findById(id);
        
        if (docOpt.isPresent()) {
            Doctor doctor = docOpt.get();
            
            // Update Doctor Profile
            doctor.setName(request.getName());
            doctor.setSpecialization(request.getSpecialization());
            doctorRepository.save(doctor);

            // Update Linked User Password
            Long userId = doctor.getDoctorId();
            if(userId != null) {
                Optional<User> userOpt = userRepository.findById(userId);
                if(userOpt.isPresent() && request.getPassword() != null && !request.getPassword().isEmpty()) {
                    User user = userOpt.get();
                    user.setPassword(request.getPassword());
                    userRepository.save(user);
                }
            }
            return ResponseEntity.ok("Doctor updated successfully");
        }
        return ResponseEntity.notFound().build();
    }

    // --- 4. DELETE DOCTOR (The Safe Way) ---
    @DeleteMapping("/doctor/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Doctor doctor = doctorOpt.get();
        Long userTableId = doctor.getDoctorId(); 

        try {
            // ✅ Step A: Delete Appointments first
            // This prevents the "Foreign Key Constraint" error
            appointmentRepository.deleteByDoctorId(id); 

            // ✅ Step B: Delete Doctor Profile
            doctorRepository.deleteById(id);

            // ✅ Step C: Delete User Login
            if (userTableId != null) {
                userRepository.deleteById(userTableId);
            }

            return ResponseEntity.ok("Doctor and linked User account deleted successfully.");
            
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.badRequest().body("Error deleting doctor: " + e.getMessage());
        }
    }

    // --- 5. UPDATE PATIENT ---
    @PutMapping("/patient/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @RequestBody User updatedData) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedData.getUsername()); 
            user.setVerified(updatedData.isVerified());  
            if(updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()){
                user.setPassword(updatedData.getPassword());
            }
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

 // --- 6. DELETE USER (Fixed for Patients) ---
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Safety Check: Do not delete Doctors here
            if ("DOCTOR".equals(user.getRole())) {
                return ResponseEntity.badRequest().body("Please delete Doctors from the 'Manage Doctors' tab.");
            }

            try {
                // ✅ STEP 1: Delete this Patient's Appointments first
                // (We use their username/email because that is likely stored in the appointment)
                appointmentRepository.deleteByPatientName(user.getUsername());
                
                // If your appointment table uses 'name' instead of 'username', use:
                // appointmentRepository.deleteByPatientName(user.getName());

                // ✅ STEP 2: Now it is safe to delete the Patient
                userRepository.deleteById(id);
                
                return ResponseEntity.ok("Patient and their appointments deleted successfully");

            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error deleting patient: " + e.getMessage());
            }
        }
        return ResponseEntity.status(404).body("User not found");
    }

    // --- 7. STATS & UTILS ---
    @DeleteMapping("/reset-queue")
    public void resetQueue() {
        appointmentRepository.deleteAll();
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        long totalPatients = userRepository.countByRole("PATIENT");
        long totalDoctors = userRepository.countByRole("DOCTOR");
        long totalTokens = appointmentRepository.count();

        return ResponseEntity.ok(Map.of(
            "totalPatients", totalPatients,
            "totalDoctors", totalDoctors,
            "totalTokens", totalTokens
        ));
    }
    
    @GetMapping("/appointments")
    public List<com.example.queuemanagement.model.Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}