package com.example.queuemanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.queuemanagement.model.Appointment;
import com.example.queuemanagement.repository.AppointmentRepository; // ✅ FIXED IMPORT (was AppoimentRepository)

@Service
public class QueueService {

    @Autowired
    private AppointmentRepository appointmentRepository; // ✅ FIXED VARIABLE TYPE

    // Example methods (Keep whatever logic you had, just fix the repository usage)
    
    public Appointment bookToken(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}