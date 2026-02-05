package com.example.queuemanagement.dto;

public class PrescriptionRequest {
    private String patientName;
    private String email;
    private String doctorName;
    private String diagnosis;
    private String medicines; // Multi-line text for medicines
    private String notes;

    // Getters and Setters
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getMedicines() { return medicines; }
    public void setMedicines(String medicines) { this.medicines = medicines; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}