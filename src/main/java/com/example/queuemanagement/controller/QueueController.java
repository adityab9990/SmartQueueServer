package com.example.queuemanagement.controller;

import java.util.List;
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

@RestController
@RequestMapping("/api/queue")
@CrossOrigin(origins = "http://localhost:3000") // Allow React Frontend
public class QueueController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // ==========================================
    // 1. PATIENT ENDPOINTS
    // ==========================================

    // Get all doctors for the dropdown
    @GetMapping("/doctors")
    public List<Doctor> getAvailableDoctors() {
        return doctorRepository.findAll();
    }

    // Book Appointment
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

        // Calculate Token: Find max token for this specific doctor
        Integer maxToken = appointmentRepository.findMaxTokenByDoctor(doctor.getId());
        int nextToken = (maxToken == null ? 0 : maxToken) + 1;

        Appointment appt = new Appointment();
        appt.setPatientName(request.getPatientName());
        appt.setDoctor(doctor);
        appt.setTokenNumber(nextToken);
        appt.setStatus("WAITING");

        return ResponseEntity.ok(appointmentRepository.save(appt));
    }

    // Get Patient Status
    @GetMapping("/patient/{name}")
    public ResponseEntity<?> getPatientStatus(@PathVariable String name) {
        Appointment appt = appointmentRepository.findFirstByPatientNameAndStatusNot(name, "COMPLETED");
        if (appt == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(appt);
    }

    // Get History
    @GetMapping("/history/{name}")
    public List<Appointment> getPatientHistory(@PathVariable String name) {
        return appointmentRepository.findByPatientName(name);
    }

    // Global "Now Serving" (Optional)
    @GetMapping("/current-serving")
    public ResponseEntity<?> getCurrentServing() {
        Appointment current = appointmentRepository.findFirstByStatusOrderByTokenNumberAsc("ACTIVE");
        return ResponseEntity.ok(current == null ? 0 : current.getTokenNumber());
    }

    // ==========================================
    // 2. DOCTOR DASHBOARD ENDPOINTS
    // ==========================================

    // ✅ Get Doctor Profile by Username (Links Login -> Doctor Profile)
    @GetMapping("/doctor-profile/{username}")
    public ResponseEntity<?> getDoctorProfile(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        // Assuming Doctor table has a 'doctor_id' column linked to User ID
        Doctor doctor = doctorRepository.findByDoctorId(user.getId()).orElse(null);
        
        if (doctor == null) {
            return ResponseEntity.badRequest().body("Profile not found. Is this user linked to a doctor?");
        }
        return ResponseEntity.ok(doctor);
    }

    // ✅ Get Waiting List for Specific Doctor
    @GetMapping("/doctor/{doctorId}/waiting")
    public List<Appointment> getDoctorWaitingList(@PathVariable Long doctorId) {
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, "WAITING");
    }

    // ✅ Get Current Active Patient for Specific Doctor
    @GetMapping("/doctor/{doctorId}/current")
    public ResponseEntity<?> getDoctorCurrentPatient(@PathVariable Long doctorId) {
        List<Appointment> activeList = appointmentRepository.findByDoctorIdAndStatus(doctorId, "ACTIVE");
        if (activeList.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(activeList.get(0));
    }

    // ✅ Update Status (Call Next / Mark Completed)
    @PutMapping("/appointment/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Appointment> apptOpt = appointmentRepository.findById(id);
        if (apptOpt.isEmpty()) return ResponseEntity.notFound().build();

        Appointment appt = apptOpt.get();
        appt.setStatus(status); // "ACTIVE" or "COMPLETED"
        return ResponseEntity.ok(appointmentRepository.save(appt));
    }

    // ==========================================
    // DTO CLASS
    // ==========================================
    public static class BookingRequest {
        private String patientName;
        private Long doctorId;

        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    }
}