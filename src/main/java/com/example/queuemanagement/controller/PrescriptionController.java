package com.example.queuemanagement.controller;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.queuemanagement.service.EmailService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@RestController
@RequestMapping("/api/prescription")
@CrossOrigin(origins = "http://localhost:3000")
public class PrescriptionController {

    @Autowired
    private EmailService emailService;

    // ==========================================
    // 1. DOWNLOAD PDF (Browser)
    // ==========================================
    @GetMapping("/download/{patientName}")
    public ResponseEntity<byte[]> downloadPrescription(
            @PathVariable String patientName,
            @RequestParam(defaultValue = "Dr. General") String doctorName,
            @RequestParam(defaultValue = "General Checkup") String diagnosis) {

        try {
            byte[] pdfBytes = generatePdfBytes(patientName, doctorName, diagnosis);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", patientName + "_Prescription.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // 2. ✅ NEW: EMAIL PDF
    // ==========================================
    @PostMapping("/email")
    public ResponseEntity<?> emailPrescription(
            @RequestParam String email,
            @RequestParam String patientName,
            @RequestParam String doctorName,
            @RequestParam String diagnosis) {

        try {
            // 1. Generate PDF
            byte[] pdfBytes = generatePdfBytes(patientName, doctorName, diagnosis);

            // 2. Send Email
            String subject = "Prescription from " + doctorName;
            String body = "Dear " + patientName + ",\n\nPlease find your prescription attached.\n\nGet well soon,\nSmart Hospital Team";
            
            emailService.sendEmailWithAttachment(email, subject, body, pdfBytes, "Prescription.pdf");

            return ResponseEntity.ok("Email Sent Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        }
    }

    // ==========================================
    // HELPER: PDF GENERATION LOGIC
    // ==========================================
    private byte[] generatePdfBytes(String patientName, String doctorName, String diagnosis) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        Paragraph header = new Paragraph("🏥 SMART CITY HOSPITAL", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        document.add(new Paragraph("\n----------------------------------------------------------------------------------\n"));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        addTableRow(table, "Doctor Name:", doctorName);
        addTableRow(table, "Patient Name:", patientName);
        addTableRow(table, "Diagnosis:", diagnosis);
        addTableRow(table, "Date:", new Date().toString());
        document.add(table);

        document.add(new Paragraph("\n\n💊 Prescribed Medicines:\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        
        com.itextpdf.text.List list = new com.itextpdf.text.List(true, false, 20);
        list.add(new ListItem("Paracetamol 500mg - (Morning, Night)"));
        list.add(new ListItem("Amoxicillin 250mg - (Afternoon)"));
        list.add(new ListItem("Vitamin C - (Morning)"));
        document.add(list);

        document.add(new Paragraph("\n\n\n\n(Signature)\n" + doctorName));
        
        document.close();
        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        cell1.setBorder(0);
        table.addCell(cell1);
        PdfPCell cell2 = new PdfPCell(new Phrase(value));
        cell2.setBorder(0);
        table.addCell(cell2);
    }
}
