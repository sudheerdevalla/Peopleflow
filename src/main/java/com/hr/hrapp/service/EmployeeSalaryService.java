package com.hr.hrapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.EmployeeAttendance;
import com.hr.hrapp.repository.EmployeeAttendanceRepository;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.dto.EmployeePayslip;

@Service
public class EmployeeSalaryService {

    @Autowired
    private EmployeeRepository repo;

    @Autowired
    private EmployeeAttendanceRepository attendanceRepo;

    @Autowired
    private EmployeePayslipPdfService pdfService;

    // ================= PDF =================
    public byte[] generatePayslipPdf(Long id, int leaves) {

        Employee emp = getEmployeeById(id);
        EmployeePayslip payslip = calculateSalary(emp, leaves);

        return pdfService.generatePayslipPdf(payslip, id);
    }

    // ================= GET EMPLOYEE =================
    public Employee getEmployeeById(Long id) {
        return repo.findById(id).orElse(null);
    }

    // ================= SIMPLE SALARY =================
    public EmployeePayslip calculateSalary(Employee emp, int leaves) {

        double basicSalary = emp.getBasicSalary();
        double hra = basicSalary * 0.2;
        double pf = basicSalary * 0.12;

        double deduction = leaves * 500;
        double netSalary = basicSalary + hra - pf - deduction;

        EmployeePayslip payslip = new EmployeePayslip();

        payslip.setId(emp.getEmpId().intValue()); // DTO uses Integer
        payslip.setName(emp.getName());
        payslip.setBasicSalary(basicSalary);
        payslip.setHra(hra);
        payslip.setPf(pf);
        payslip.setLeaveDeduction(deduction);
        payslip.setNetSalary(netSalary);

        // EXTRA FIELDS
        payslip.setRole(emp.getRole());
        payslip.setLocation(emp.getLocation());
        payslip.setBankName(emp.getBankName());
        payslip.setAccountNumber(emp.getAccountNumber());
        payslip.setIfsc(emp.getIfsc());

        return payslip;
    }

    // ================= CRUD =================
    public Employee saveEmployee(Employee emp) {
        return repo.save(emp);
    }

    public List<Employee> getAllEmployees() {
        return repo.findAll();
    }

    public void deleteEmployee(Long id) {
        repo.deleteById(id);
    }

    public Employee updateEmployee(Long id, Employee emp) {

        Employee existing = repo.findById(id).orElse(null);

        if (existing != null) {
            existing.setName(emp.getName());
            existing.setBasicSalary(emp.getBasicSalary());
            existing.setDepartment(emp.getDepartment());
            existing.setEmail(emp.getEmail());
        }

        return repo.save(existing);
    }

    // ================= ADVANCED PAYSLIP =================
    public EmployeePayslip generatePayslip(Long id) {

        Employee emp = repo.findById(id).orElse(null);

        if (emp == null) {
            throw new RuntimeException("Employee not found");
        }

        EmployeeAttendance att = attendanceRepo.findByEmployeeId(id);

        if (att == null) {
            throw new RuntimeException("Attendance not found for employee id: " + id);
        }

        int totalDays = att.getTotalDays();
        int presentDays = att.getPresentDays();
        int leaves = totalDays - presentDays;

        double basic = emp.getBasicSalary();
        double hra = basic * 0.2;
        double pf = basic * 0.12;

        double perDay = basic / totalDays;
        double deduction = leaves * perDay;

        double net = basic + hra - pf - deduction;
        net = Math.round(net * 100.0) / 100.0;

        EmployeePayslip p = new EmployeePayslip();
        
        if(emp.getEmpId() != null) {
        p.setId(emp.getEmpId().intValue());
        }else {
        	p.setId(0);
        }
        p.setName(emp.getName());
        p.setBasicSalary(basic);
        p.setHra(hra);
        p.setPf(pf);
        p.setLeaveDeduction(deduction);
        p.setNetSalary(net);

        // EXTRA FIELDS
        p.setRole(emp.getRole());
        p.setLocation(emp.getLocation());
        p.setBankName(emp.getBankName());
        p.setAccountNumber(emp.getAccountNumber());
        p.setIfsc(emp.getIfsc());

        return p;
    }
}