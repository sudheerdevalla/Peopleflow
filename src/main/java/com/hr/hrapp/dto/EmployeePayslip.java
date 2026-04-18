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


}
