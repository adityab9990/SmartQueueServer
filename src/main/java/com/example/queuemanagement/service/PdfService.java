package com.example.queuemanagement.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.example.queuemanagement.dto.PrescriptionRequest;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class PdfService {

    public byte[] generatePrescriptionPdf(PrescriptionRequest request) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLUE);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);

            // Header
            Paragraph title = new Paragraph("Smart Hospital Prescription", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Patient & Doctor Details
            document.add(new Paragraph("Doctor: Dr. " + request.getDoctorName(), headerFont));
            document.add(new Paragraph("Patient: " + request.getPatientName(), headerFont));
            document.add(new Paragraph("Date: " + new java.util.Date(), bodyFont));
            document.add(new Paragraph("----------------------------------------------------------"));

            // Diagnosis
            document.add(new Paragraph("Diagnosis:", headerFont));
            document.add(new Paragraph(request.getDiagnosis(), bodyFont));
            document.add(new Paragraph("\n"));

            // Medicines
            document.add(new Paragraph("Rx (Medicines):", headerFont));
            document.add(new Paragraph(request.getMedicines(), bodyFont)); 
            document.add(new Paragraph("\n"));

            // Notes
            if (request.getNotes() != null && !request.getNotes().isEmpty()) {
                document.add(new Paragraph("Advice / Notes:", headerFont));
                document.add(new Paragraph(request.getNotes(), bodyFont));
            }

            // Footer
            document.add(new Paragraph("\n\n\n"));
            Paragraph footer = new Paragraph("Signed By: Dr. " + request.getDoctorName(), bodyFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}