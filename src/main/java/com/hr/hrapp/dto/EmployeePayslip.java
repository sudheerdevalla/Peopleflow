package com.hr.hrapp.dto;

public class EmployeePayslip {
	private Integer id;
	private String name;
    private double basicSalary;
    private double hra;
    private double pf;
    private double leaveDeduction;
    private double netSalary;
    private String role;
    private String location;
    private String bankName;
    private String accountNumber;
    private String ifsc;
    
    private double travelAllowance;
    
    private double conveyance;
    private double telephone;
    private double internet;
    private double specialAllowance;

    private double grossEarning;

    private double employeeEsi;
    private double professionalTax;
    private double tds;
    private double groupHealthInsurance;
    private double advanceSalaryRecovery;

    private double totalDeductions;

    private double employerPf;
    private double employerEps;
    private double employerEsi;
    private double employerPfAdmin;
    private double edli;

    private double totalEmployerContribution;

    private double ctc;
    
    public Integer getId() {
    	return id;
    }
    public void setId(Integer id) {
    	this.id=id;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }

    public double getHra() { return hra; }
    public void setHra(double hra) { this.hra = hra; }

    public double getPf() { return pf; }
    public void setPf(double pf) { this.pf = pf; }

    public double getLeaveDeduction() { return leaveDeduction; }
    public void setLeaveDeduction(double leaveDeduction) { this.leaveDeduction = leaveDeduction; }

    public double getNetSalary() { return netSalary; }
    public void setNetSalary(double netSalary) { this.netSalary = netSalary; }
    
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }
    
    public double getTravelAllowance() {
        return travelAllowance;
    }
    public void setTravelAllowance(double travelAllowance) {
        this.travelAllowance = travelAllowance;
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

    public double getGrossEarning() {
        return grossEarning;
    }

    public void setGrossEarning(double grossEarning) {
        this.grossEarning = grossEarning;
    }

    public double getEmployeeEsi() {
        return employeeEsi;
    }

    public void setEmployeeEsi(double employeeEsi) {
        this.employeeEsi = employeeEsi;
    }

    public double getProfessionalTax() {
        return professionalTax;
    }

    public void setProfessionalTax(double professionalTax) {
        this.professionalTax = professionalTax;
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

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(double totalDeductions) {
        this.totalDeductions = totalDeductions;
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


}
