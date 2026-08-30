package com.hr.hrapp.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empId;

    // =========================
    // BASIC DETAILS
    // =========================
    private String profilePhoto;

    private String name;
    
   

    private String email;

    private String department;

    private String role;

    private String designation;

    private String status;

    // =========================
    // PROFILE DETAILS
    // =========================

    private String experience;

    private String previousExperience;

    private String location;

    private LocalDate joiningDate;

    // =========================
    // LEAVE DETAILS
    // =========================

    private int leaves;

    private int sickLeaves = 6;

    private double annualLeaves = 0;

    private LocalDate lastAccrualDate;

    // =========================
    // FINANCIAL DETAILS
    // =========================

    private String bankName;

    private String accountNumber;

    private String ifsc;

    private String panNumber;

    private LocalDate dateOfBirth;

    // =========================
    // PAYROLL DETAILS
    // =========================

    private double basicSalary;
    private double totalCtc;

    private String pfNumber;

    private String uanNumber;

    private Double hraPercentage;

    private Double bonusPercentage;
    
    private String employeeCode;
    private String fatherName;
    private String gender;
    private String aadhaarNumber;
    private String esiNumber;

    private String permanentAddress;
    private String correspondenceAddress;

    private String maritalStatus;
    private String spouseName;

    private String shift;
    private String paymentMode;
    
    private String mobile;
    private String branch;
    private LocalDate confirmationDate;
    private Integer probationPeriod;
    
    private Double travelAllowance;

    // =========================
    // MANAGER MAPPING
    // =========================

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    // =========================
    // SUBORDINATES
    // =========================

    @JsonIgnore
    @OneToMany(mappedBy = "manager", fetch = jakarta.persistence.FetchType.LAZY)
    private List<Employee> subordinates =
            new ArrayList<>();

    // =========================
    // CONSTRUCTORS
    // =========================

    public Employee() {
    }

    public Employee(String name,
                    String department,
                    double basicSalary) {

        this.name = name;
        this.department = department;
        this.basicSalary = basicSalary;
    }

    // =========================
    // GETTERS & SETTERS
    // =========================
    
    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public Long getEmpId() {
        return empId;
    }

    public void setEmpId(Long empId) {
        this.empId = empId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getPreviousExperience() {
        return previousExperience;
    }

    public void setPreviousExperience(
            String previousExperience) {

        this.previousExperience =
                previousExperience;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(
            LocalDate joiningDate) {

        this.joiningDate = joiningDate;
    }

    public int getLeaves() {
        return leaves;
    }

    public void setLeaves(int leaves) {
        this.leaves = leaves;
    }

    public int getSickLeaves() {
        return sickLeaves;
    }

    public void setSickLeaves(int sickLeaves) {
        this.sickLeaves = sickLeaves;
    }

    public double getAnnualLeaves() {
        return annualLeaves;
    }

    public void setAnnualLeaves(
            double annualLeaves) {

        this.annualLeaves = annualLeaves;
    }

    public LocalDate getLastAccrualDate() {
        return lastAccrualDate;
    }

    public void setLastAccrualDate(
            LocalDate lastAccrualDate) {

        this.lastAccrualDate = lastAccrualDate;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(
            String accountNumber) {

        this.accountNumber = accountNumber;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(
            String panNumber) {

        this.panNumber = panNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(
            LocalDate dateOfBirth) {

        this.dateOfBirth = dateOfBirth;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(
            double basicSalary) {

        this.basicSalary = basicSalary;
    }
      public double getTotalCtc() {
    return totalCtc;
}

public void setTotalCtc(double totalCtc) {
    this.totalCtc = totalCtc;
}

    public String getPfNumber() {
        return pfNumber;
    }

    public void setPfNumber(String pfNumber) {
        this.pfNumber = pfNumber;
    }

    public String getUanNumber() {
        return uanNumber;
    }

    public void setUanNumber(String uanNumber) {
        this.uanNumber = uanNumber;
    }

    public Double getHraPercentage() {
        return hraPercentage;
    }

    public void setHraPercentage(
            Double hraPercentage) {

        this.hraPercentage = hraPercentage;
    }

    public Double getBonusPercentage() {
        return bonusPercentage;
    }

    public void setBonusPercentage(
            Double bonusPercentage) {

        this.bonusPercentage = bonusPercentage;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public List<Employee> getSubordinates() {
        return subordinates;
    }

    public void setSubordinates(
            List<Employee> subordinates) {

        this.subordinates = subordinates;
    }
    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public String getEsiNumber() {
        return esiNumber;
    }

    public void setEsiNumber(String esiNumber) {
        this.esiNumber = esiNumber;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getCorrespondenceAddress() {
        return correspondenceAddress;
    }

    public void setCorrespondenceAddress(String correspondenceAddress) {
        this.correspondenceAddress = correspondenceAddress;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public LocalDate getConfirmationDate() {
        return confirmationDate;
    }

    public void setConfirmationDate(LocalDate confirmationDate) {
        this.confirmationDate = confirmationDate;
    }

    public Integer getProbationPeriod() {
        return probationPeriod;
    }

    public void setProbationPeriod(Integer probationPeriod) {
        this.probationPeriod = probationPeriod;
    }
    
    public Double getTravelAllowance() {
        return travelAllowance;
    }

    public void setTravelAllowance(Double travelAllowance) {
        this.travelAllowance = travelAllowance;
    }
}
