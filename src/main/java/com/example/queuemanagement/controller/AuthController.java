package com.example.queuemanagement.controller;

import java.util.Map;
import java.util.Random;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.queuemanagement.model.User;
import com.example.queuemanagement.repository.UserRepository;
import com.example.queuemanagement.service.EmailService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Allow Frontend Access
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // ==========================================
    // 1. REGISTER (Updated to save Name & Mobile)
    // ==========================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        
        // 1. Check if user already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Email/Username already exists");
        }

        // 2. Ensure Role is set (Default to PATIENT if missing)
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("PATIENT");
        }
        
        // ✅ 3. EXPLICITLY SET NAME AND MOBILE (Just to be safe)
        // This ensures the JSON data from React is actually used
        user.setName(user.getName());
        user.setMobile(user.getMobile());

        // 4. PATIENT Logic: Needs OTP Verification
        if ("PATIENT".equalsIgnoreCase(user.getRole())) {
            String otp = String.valueOf(new Random().nextInt(9000) + 1000); // Generate 4-digit OTP
            user.setOtp(otp);
            user.setVerified(false); // Mark as NOT verified
            userRepository.save(user); // Save to DB

            try {
                emailService.sendOtpEmail(user.getUsername(), otp);
                return ResponseEntity.ok("OTP_SENT");
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Error sending email");
            }
        } 
        
        // 5. DOCTOR / ADMIN Logic: Auto-Verify (No OTP needed)
        else {
            user.setVerified(true); // Automatically verified
            user.setOtp(null);      // No OTP needed
            userRepository.save(user);
            return ResponseEntity.ok("REGISTERED_DIRECTLY");
        }
    }

    // ==========================================
    // 2. VERIFY OTP (Only for Patients)
    // ==========================================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        User user = userRepository.findByUsername(email).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Check if OTP matches
        if (otp != null && otp.equals(user.getOtp())) {
            user.setVerified(true);
            user.setOtp(null); // Clear OTP after use
            userRepository.save(user);
            return ResponseEntity.ok("Verification Successful!");
        }
        
        return ResponseEntity.badRequest().body("Invalid OTP");
    }

    // ==========================================
    // 3. LOGIN (Checks Verification)
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User user = userOpt.get();

        // 1. Check Password (Simple check)
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(401).body("Invalid Credentials");
        }

        // 2. Check Verification Status
        // Patients must be verified. Doctors/Admins are auto-verified in Register step.
        if (!user.isVerified()) {
            return ResponseEntity.status(403).body("Account not verified. Please verify OTP first.");
        }

        return ResponseEntity.ok(user);
    }
    
    // DTO Class for clean Login Requests
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    // ==========================================
    // 4. GOOGLE LOGIN (Updated to save Name)
    // ==========================================
    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String name = request.get("name"); // React sends "name"

        // 1. Check if user exists
        Optional<User> userOpt = userRepository.findByUsername(email);

        if (userOpt.isPresent()) {
            // User exists -> Log them in directly
            return ResponseEntity.ok(userOpt.get());
        } else {
            // User doesn't exist -> Register them automatically as PATIENT
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setPassword("GOOGLE_USER"); // Dummy password
            newUser.setRole("PATIENT");
            
            // ✅ Save the Name from Google
            newUser.setName(name); 
            
            newUser.setVerified(true); // Google emails are already verified
            
            userRepository.save(newUser);
            return ResponseEntity.ok(newUser);
        }
    }
}