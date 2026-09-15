package com.hr.hrapp.payroll.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;

    private String employeeName;

    private double basicSalary;

    private double hra;

    private double conveyance;
    
    private double telephone;
    
    private double internet;
    
    private Double travelAllowance;
    
    private double specialAllowance;

    private double pf;
    private double employeeEsi;
    private double professionalTax;
    private double tds;
    private double groupHealthInsurance;
    private double advanceSalaryRecovery;
    private double deductions;
    private double totalDeductions;

    private double employerPf;
    private double employerEps;
    private double employerEsi;
    private double employerPfAdmin;
    private double edli;
    private double totalEmployerContribution;

    private double ctc;
    
    private double approvedAdditions;

    private double grossSalary;
    
    private double grossEarning;

    private int payableDays;

    private int workingDays;

    private double netSalary;

    private String month;

    private String status = "DRAFT";

    private String reconciliationStatus = "PENDING";

    private LocalDateTime lastCalculatedAt;

    private LocalDateTime finalizedAt;

    private String finalizedBy;

    // =========================
    // GETTERS & SETTERS
    // =========================

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

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(
            String employeeName) {

        this.employeeName = employeeName;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(
            double basicSalary) {

        this.basicSalary = basicSalary;
    }

    public double getHra() {
        return hra;
    }

    public void setHra(double hra) {
        this.hra = hra;
    }
    
    public double getConveyance() {
        return conveyance;
    }

    public void setConveyance(double conveyance) {
        this.conveyance = conveyance;
    }

    public double getTelephone() {
        return telephone;
    }

    public void setTelephone(double telephone) {
        this.telephone = telephone;
    }

    public double getInternet() {
        return internet;
    }
    
    public void setInternet(double internet) {
        this.internet = internet;
    }

    public double getSpecialAllowance() {
        return specialAllowance;
    }

    public void setSpecialAllowance(double specialAllowance) {
        this.specialAllowance = specialAllowance;
    }
    
    public Double getTravelAllowance() {
        return travelAllowance;
    }

    public void setTravelAllowance(Double travelAllowance) {
        this.travelAllowance = travelAllowance;
    }

    public double getPf() {
        return pf;
    }

    public void setPf(double pf) {
        this.pf = pf;
    }
    
    public double getEmployeeEsi() {
        return employeeEsi;
    }

    public void setEmployeeEsi(double employeeEsi) {
        this.employeeEsi = employeeEsi;
    }

    public double getTds() {
        return tds;
    }

    public void setTds(double tds) {
        this.tds = tds;
    }

    public double getGroupHealthInsurance() {
        return groupHealthInsurance;
    }

    public void setGroupHealthInsurance(double groupHealthInsurance) {
        this.groupHealthInsurance = groupHealthInsurance;
    }

    public double getAdvanceSalaryRecovery() {
        return advanceSalaryRecovery;
    }

    public void setAdvanceSalaryRecovery(double advanceSalaryRecovery) {
        this.advanceSalaryRecovery = advanceSalaryRecovery;
    }
    
    public double getEmployerPf() {
        return employerPf;
    }

    public void setEmployerPf(double employerPf) {
        this.employerPf = employerPf;
    }
    
    public double getEmployerEps() {
        return employerEps;
    }

    public void setEmployerEps(double employerEps) {
        this.employerEps = employerEps;
    }

    public double getEmployerEsi() {
        return employerEsi;
    }

    public void setEmployerEsi(double employerEsi) {
        this.employerEsi = employerEsi;
    }

    public double getEmployerPfAdmin() {
        return employerPfAdmin;
    }

    public void setEmployerPfAdmin(double employerPfAdmin) {
        this.employerPfAdmin = employerPfAdmin;
    }

    public double getEdli() {
        return edli;
    }

    public void setEdli(double edli) {
        this.edli = edli;
    }

    public double getTotalEmployerContribution() {
        return totalEmployerContribution;
    }

    public void setTotalEmployerContribution(double totalEmployerContribution) {
        this.totalEmployerContribution = totalEmployerContribution;
    }

    public double getCtc() {
        return ctc;
    }

    public void setCtc(double ctc) {
        this.ctc = ctc;
    }
    
    public double getProfessionalTax() {
        return professionalTax;
    }

    public void setProfessionalTax(double professionalTax) {
        this.professionalTax = professionalTax;
    }
    

    public double getDeductions() {
        return deductions;
    }

    public void setDeductions(
            double deductions) {

        this.deductions = deductions;
    }

    public double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(
            double netSalary) {

        this.netSalary = netSalary;
    }

    public double getApprovedAdditions() {
        return approvedAdditions;
    }

    public void setApprovedAdditions(double approvedAdditions) {
        this.approvedAdditions = approvedAdditions;
    }

    public double getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(double grossSalary) {
        this.grossSalary = grossSalary;
    }
    
    public double getGrossEarning() {
        return grossEarning;
    }

    public void setGrossEarning(double grossEarning) {
        this.grossEarning = grossEarning;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(double totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public int getPayableDays() {
        return payableDays;
    }

    public void setPayableDays(int payableDays) {
        this.payableDays = payableDays;
    }

    public int getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(int workingDays) {
        this.workingDays = workingDays;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReconciliationStatus() {
        return reconciliationStatus;
    }

    public void setReconciliationStatus(String reconciliationStatus) {
        this.reconciliationStatus = reconciliationStatus;
    }

    public LocalDateTime getLastCalculatedAt() {
        return lastCalculatedAt;
    }

    public void setLastCalculatedAt(LocalDateTime lastCalculatedAt) {
        this.lastCalculatedAt = lastCalculatedAt;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public String getFinalizedBy() {
        return finalizedBy;
    }

    public void setFinalizedBy(String finalizedBy) {
        this.finalizedBy = finalizedBy;
    }
}