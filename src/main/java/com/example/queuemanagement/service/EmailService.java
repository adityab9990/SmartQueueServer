package com.example.queuemanagement.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 1. OTP Email
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("adityabhalerao2818@gmail.com"); 
        message.setTo(toEmail);
        message.setSubject("Hospital Queue - Verify Your Registration");
        message.setText("Your OTP for registration is: " + otp + "\n\nThis code is valid for 5 minutes.");
        
        mailSender.send(message);
        System.out.println("OTP Mail sent to " + toEmail);
    }

    // 2. Email with Attachment (Prescription)
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] pdfData, String filename) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        helper.setFrom("adityabhalerao2818@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        helper.addAttachment(filename, new ByteArrayResource(pdfData));

        mailSender.send(message);
        System.out.println("PDF Mail sent to " + to);
    }

    public void sendBookingConfirmation(String toEmail, String patientName, String doctorName, int tokenNumber) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("adityabhalerao2818@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Appointment Confirmed - Token #" + tokenNumber);
        
        String body = "Dear " + patientName + ",\n\n"
                + "Your appointment has been successfully booked!\n\n"
                + "🎫 Token Number: " + tokenNumber + "\n"
                + "👨‍⚕️ Doctor: " + doctorName + "\n\n"
                + "Please reach the clinic on time and wait for your turn.\n"
                + "You can track your status live on your dashboard.\n\n"
                + "Regards,\nSmart Hospital Team";

        message.setText(body);
        
        mailSender.send(message);
        System.out.println("Booking Confirmation sent to " + toEmail);
    }
}