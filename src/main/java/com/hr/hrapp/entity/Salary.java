package com.hr.hrapp.entity;

import jakarta.persistence.*;

@Entity
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;

    private String month;

    private double basicSalary;   // ✅ important
    private double netSalary;     // ✅ important
    private double hikeAmount;

    // ===== GETTERS =====

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getMonth() {
        return month;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public double getNetSalary() {
        return netSalary;
    }
    public double getHikeAmount() {
    	return hikeAmount;
    }

    // ===== SETTERS =====

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setBasicSalary(double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public void setNetSalary(double netSalary) {
        this.netSalary = netSalary;
    }
    
    public void setHikeAmount (double hikeAmount) {
    	this.hikeAmount = hikeAmount;
    }
}