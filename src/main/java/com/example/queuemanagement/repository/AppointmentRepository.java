package com.example.queuemanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.queuemanagement.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ✅ THIS METHOD IS REQUIRED for the Patient Dashboard
    List<Appointment> findByPatientName(String patientName);

    // Finds the next person in the queue
    Appointment findFirstByStatusOrderByTokenNumberAsc(String status);
    
    // (Keep your other existing methods below...)
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);
    
    @Query("SELECT MAX(a.tokenNumber) FROM Appointment a WHERE a.doctor.id = :doctorId")
    Integer findMaxTokenByDoctor(@Param("doctorId") Long doctorId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId")
    void deleteByDoctorId(@Param("doctorId") Long doctorId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.patientName = :patientName")
    void deleteByPatientName(@Param("patientName") String patientName);

	Appointment findFirstByPatientNameAndStatusNot(String patientName, String string);
}