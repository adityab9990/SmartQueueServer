package com.example.queuemanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.example.queuemanagement.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);
    List<Appointment> findByPatientName(String patientName);

    @Query("SELECT MAX(a.tokenNumber) FROM Appointment a WHERE a.doctor.id = :doctorId")
    Integer findMaxTokenByDoctor(@Param("doctorId") Long doctorId);

    Appointment findFirstByPatientNameAndStatusNot(String patientName, String status);
    
    // ✅ EXISTING: Delete Doctor's Appointments
    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId")
    void deleteByDoctorId(@Param("doctorId") Long doctorId);

    // ✅ NEW: Delete Patient's Appointments (Fixes your issue)
    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.patientName = :patientName")
    void deleteByPatientName(@Param("patientName") String patientName);
	Appointment findFirstByStatusOrderByTokenNumberAsc(String string);
}