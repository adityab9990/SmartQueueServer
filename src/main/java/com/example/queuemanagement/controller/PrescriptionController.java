package com.example.queuemanagement.controller;

import com.example.queuemanagement.dto.PrescriptionRequest;
import com.example.queuemanagement.service.EmailService;
import com.example.queuemanagement.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prescription")
@CrossOrigin(origins = "http://localhost:3000")
public class PrescriptionController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmailService emailService;

    // ==========================================
    // 1. ✅ EMAIL the Prescription
    // ==========================================
    @PostMapping("/send")
    public ResponseEntity<?> sendPrescription(@RequestBody PrescriptionRequest request) {
        try {
            // 1. Generate PDF using the new PdfService
            byte[] pdfBytes = pdfService.generatePrescriptionPdf(request);
            
            // 2. Send Email
            emailService.sendEmailWithAttachment(
                request.getEmail(),
                "Prescription - Dr. " + request.getDoctorName(),
                "Dear " + request.getPatientName() + ",\n\nPlease find your prescription attached.\n\nGet well soon,\nSmart Hospital Team",
                pdfBytes,
                "Prescription.pdf"
            );
            
            return ResponseEntity.ok("Email Sent Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. ✅ DOWNLOAD the Prescription (For Doctor Verification)
    // ==========================================
    @PostMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(@RequestBody PrescriptionRequest request) {
        try {
            // 1. Generate PDF
            byte[] pdfBytes = pdfService.generatePrescriptionPdf(request);

            // 2. Return as Downloadable File
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "Prescription_" + request.getPatientName() + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ==========================================
    // 3. (Optional) Legacy GET Download - Kept for compatibility if needed
    // ==========================================
    @GetMapping("/download/{patientName}")
    public ResponseEntity<byte[]> downloadLegacy(
            @PathVariable String patientName,
            @RequestParam(defaultValue = "Dr. General") String doctorName,
            @RequestParam(defaultValue = "General Checkup") String diagnosis) {
        
        // Create a temporary request object to reuse logic
        PrescriptionRequest req = new PrescriptionRequest();
        req.setPatientName(patientName);
        req.setDoctorName(doctorName);
        req.setDiagnosis(diagnosis);
        req.setMedicines("Standard Medicines"); // Default
        
        return downloadPrescriptionPdf(req);
    }
}