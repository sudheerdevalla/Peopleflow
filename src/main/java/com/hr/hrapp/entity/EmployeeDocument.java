package com.hr.hrapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;

    private String documentType;

    private String fileName;

    private String originalFileName;

    private String contentType;

    private String storagePath;

    @Column(unique = true)
    private String fileHash;

    private String extractionStatus;

    private String validationStatus;

    private String conflictStatus;

    private String reviewStatus;

    private String extractedName;

    private String extractedEmail;

    private String extractedEmployeeCode;

    private String extractedPanNumber;

    private String extractedAadhaarNumber;

    private String extractedPfNumber;

    private String extractedUanNumber;

    private String extractedAccountNumber;

    private String extractedIfsc;

    @Column(length = 2000)
    private String reviewNotes;

    private String reviewedBy;

    private LocalDateTime reviewedAt;

    private LocalDateTime uploadedAt;

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getExtractionStatus() {
        return extractionStatus;
    }

    public void setExtractionStatus(String extractionStatus) {
        this.extractionStatus = extractionStatus;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getConflictStatus() {
        return conflictStatus;
    }

    public void setConflictStatus(String conflictStatus) {
        this.conflictStatus = conflictStatus;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getExtractedName() {
        return extractedName;
    }

    public void setExtractedName(String extractedName) {
        this.extractedName = extractedName;
    }

    public String getExtractedEmail() {
        return extractedEmail;
    }

    public void setExtractedEmail(String extractedEmail) {
        this.extractedEmail = extractedEmail;
    }

    public String getExtractedEmployeeCode() {
        return extractedEmployeeCode;
    }

    public void setExtractedEmployeeCode(String extractedEmployeeCode) {
        this.extractedEmployeeCode = extractedEmployeeCode;
    }

    public String getExtractedPanNumber() {
        return extractedPanNumber;
    }

    public void setExtractedPanNumber(String extractedPanNumber) {
        this.extractedPanNumber = extractedPanNumber;
    }

    public String getExtractedAadhaarNumber() {
        return extractedAadhaarNumber;
    }

    public void setExtractedAadhaarNumber(String extractedAadhaarNumber) {
        this.extractedAadhaarNumber = extractedAadhaarNumber;
    }

    public String getExtractedPfNumber() {
        return extractedPfNumber;
    }

    public void setExtractedPfNumber(String extractedPfNumber) {
        this.extractedPfNumber = extractedPfNumber;
    }

    public String getExtractedUanNumber() {
        return extractedUanNumber;
    }

    public void setExtractedUanNumber(String extractedUanNumber) {
        this.extractedUanNumber = extractedUanNumber;
    }

    public String getExtractedAccountNumber() {
        return extractedAccountNumber;
    }

    public void setExtractedAccountNumber(String extractedAccountNumber) {
        this.extractedAccountNumber = extractedAccountNumber;
    }

    public String getExtractedIfsc() {
        return extractedIfsc;
    }

    public void setExtractedIfsc(String extractedIfsc) {
        this.extractedIfsc = extractedIfsc;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
