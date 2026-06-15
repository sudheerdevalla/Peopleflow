package com.hr.hrapp.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TravelRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long empId;

    private String employeeName;

    private String destination;

    private LocalDate fromDate;

    private LocalDate toDate;

    private String purpose;
    
    private double estimatedCost;

    private String ticketFile;
    
    private Double travelAllowance;
    
    private boolean payrollProcessed = false;

    // Status flow: REQUESTED -> MANAGER_APPROVED -> ADMIN_APPROVED -> COMPLETED
    // Other possible values: REJECTED
    private String status = "REQUESTED";

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpId() {
        return empId;
    }

    public void setEmpId(Long empId) {
        this.empId = empId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    // ✅ Ticket File
    public String getTicketFile() {
        return ticketFile;
    }

    public void setTicketFile(String ticketFile) {
        this.ticketFile = ticketFile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public Double getTravelAllowance() {
        return travelAllowance;
    }

    public void setTravelAllowance(Double travelAllowance) {
        this.travelAllowance = travelAllowance;
    }
    
    public boolean isPayrollProcessed() {
        return payrollProcessed;
    }

    public void setPayrollProcessed(boolean payrollProcessed) {
        this.payrollProcessed = payrollProcessed;
    }
}