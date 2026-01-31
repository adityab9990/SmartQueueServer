package com.example.queuemanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("adityabhalerao2818@gmail.com"); // Must match application.properties
        message.setTo(toEmail);
        message.setSubject("Hospital Queue - Verify Your Registration");
        message.setText("Your OTP for registration is: " + otp + "\n\nThis code is valid for 5 minutes.");
        
        mailSender.send(message);
        System.out.println("Mail sent to " + toEmail);
    }
}