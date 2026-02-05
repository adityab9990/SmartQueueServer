package com.example.queuemanagement.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.queuemanagement.model.Appointment;
import com.example.queuemanagement.model.Doctor;
import com.example.queuemanagement.model.User;
import com.example.queuemanagement.repository.AppointmentRepository;
import com.example.queuemanagement.repository.DoctorRepository;
import com.example.queuemanagement.repository.UserRepository;
import com.example.queuemanagement.service.EmailService;

@RestController
@RequestMapping("/api/queue")
@CrossOrigin(origins = "http://localhost:3000")
public class QueueController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private EmailService emailService;

    // ==========================================
    // 1. PATIENT ENDPOINTS
    // ==========================================

    @GetMapping("/doctors")
    public List<Doctor> getAvailableDoctors() {
        return doctorRepository.findAll();
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody BookingRequest request) {
        if (request.getDoctorId() == null) {
            return ResponseEntity.badRequest().body("Doctor ID is required.");
        }

        Optional<Doctor> doctorOpt = doctorRepository.findById(request.getDoctorId());
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Doctor not found.");
        }
        Doctor doctor = doctorOpt.get();

        // 1. Check for duplicate booking (Exclude CANCELLED so they can re-book)
        Appointment existing = appointmentRepository.findFirstByPatientNameAndStatusNot(request.getPatientName(), "COMPLETED");
        if (existing != null && !"CANCELLED".equals(existing.getStatus())) {
             return ResponseEntity.badRequest().body("You already have an active appointment (Token #" + existing.getTokenNumber() + ")");
        }

        // 2. Calculate Token
        Integer maxToken = appointmentRepository.findMaxTokenByDoctor(doctor.getId());
        int nextToken = (maxToken == null ? 0 : maxToken) + 1;

        // 3. Create Appointment
        Appointment appt = new Appointment();
        appt.setPatientName(request.getPatientName()); 
        appt.setDoctor(doctor);
        appt.setTokenNumber(nextToken);
        appt.setStatus("WAITING");

        Appointment savedAppt = appointmentRepository.save(appt);

        // 4. Send Email (Using Real Name)
        try {
            User patientUser = userRepository.findByUsername(request.getPatientName()).orElse(null);
            String realName = (patientUser != null && patientUser.getName() != null) ? patientUser.getName() : request.getPatientName();
            
            emailService.sendBookingConfirmation(
                request.getPatientName(), // Send TO: Email
                realName,                 // Address as: Real Name
                doctor.getName(), 
                nextToken
            );
        } catch (Exception e) {
            System.err.println("Failed to send booking email: " + e.getMessage());
        }

        return ResponseEntity.ok(savedAppt);
    }

    // ✅ NEW: Cancel Appointment
    @PutMapping("/cancel/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        Optional<Appointment> apptOpt = appointmentRepository.findById(id);
        
        if (apptOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appt = apptOpt.get();
        
        // Validation: Cannot cancel if already completed
        if ("COMPLETED".equals(appt.getStatus())) {
            return ResponseEntity.badRequest().body("Cannot cancel a completed appointment.");
        }

        appt.setStatus("CANCELLED");
        appointmentRepository.save(appt);
        
        return ResponseEntity.ok("Appointment Cancelled Successfully");
    }

    @GetMapping("/patient/status/{name}")
    public ResponseEntity<?> getPatientStatus(@PathVariable String name) {
        // Find the active appointment (Not COMPLETED)
        Appointment appt = appointmentRepository.findFirstByPatientNameAndStatusNot(name, "COMPLETED");
        
        // If the found appointment is CANCELLED, treat it as "No active appointment"
        if (appt != null && "CANCELLED".equals(appt.getStatus())) {
            return ResponseEntity.ok(Map.of("message", "No active appointments"));
        }

        if (appt == null) {
            return ResponseEntity.ok(Map.of("message", "No active appointments"));
        }
        return ResponseEntity.ok(appt);
    }
    
    @GetMapping("/history/{name}")
    public ResponseEntity<?> getPatientHistory(@PathVariable String name) {
        System.out.println("🔍 Fetching History for User: " + name); 
        List<Appointment> history = appointmentRepository.findByPatientName(name);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/current-serving")
    public ResponseEntity<?> getCurrentServing() {
        Appointment current = appointmentRepository.findFirstByStatusOrderByTokenNumberAsc("ACTIVE");
        
        if (current == null) {
            current = appointmentRepository.findFirstByStatusOrderByTokenNumberAsc("WAITING");
        }

        if (current != null) {
            // ✅ Safety Check for Null Doctor
            String doctorName = (current.getDoctor() != null) ? current.getDoctor().getName() : "Unknown Doctor";
            
            return ResponseEntity.ok(Map.of(
                "token", current.getTokenNumber(),
                "doctor", doctorName
            ));
        }
        return ResponseEntity.ok(Map.of("token", "None", "doctor", "-"));
    }

    // ==========================================
    // 2. DOCTOR DASHBOARD ENDPOINTS
    // ==========================================

    @GetMapping("/doctor-profile/{username}")
    public ResponseEntity<?> getDoctorProfile(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        Optional<Doctor> doctorOpt = doctorRepository.findByDoctorId(user.getId());
        
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Profile not found.");
        }
        return ResponseEntity.ok(doctorOpt.get());
    }

    @GetMapping("/doctor/{doctorId}/waiting")
    public List<Appointment> getDoctorWaitingList(@PathVariable Long doctorId) {
        // ✅ Ensure we don't show CANCELLED patients in the doctor's list
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, "WAITING");
    }

    @GetMapping("/doctor/{doctorId}/current")
    public ResponseEntity<?> getDoctorCurrentPatient(@PathVariable Long doctorId) {
        List<Appointment> activeList = appointmentRepository.findByDoctorIdAndStatus(doctorId, "ACTIVE");
        if (activeList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(activeList.get(0));
    }

    @PutMapping("/appointment/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Appointment> apptOpt = appointmentRepository.findById(id);
        if (apptOpt.isEmpty()) return ResponseEntity.notFound().build();

        Appointment appt = apptOpt.get();
        appt.setStatus(status); 
        return ResponseEntity.ok(appointmentRepository.save(appt));
    }

    public static class BookingRequest {
        private String patientName;
        private Long doctorId;

        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    }
}