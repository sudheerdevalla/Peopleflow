package com.hr.hrapp.payroll.controller;

import java.io.ByteArrayInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.payroll.service.PayrollMailService;
import com.hr.hrapp.payroll.service.PayrollService;
import com.hr.hrapp.payroll.util.PayslipGenerator;
import com.hr.hrapp.repository.EmployeeRepository;

@Controller
@RequestMapping("/payroll")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PayrollMailService payrollMailService;

    // =========================
    // GENERATE PAYROLL
    // =========================

    @GetMapping("/generate/{id}")
    public String generatePayroll(
            @PathVariable Long id) {

        Employee employee =
                employeeRepository
                .findById(id)
                .orElse(null);

        if(employee == null) {

            return "Employee Not Found";
        }

        // =========================
        // GENERATE PAYROLL
        // =========================

        Payroll payroll =
                payrollService
                .calculateSalary(employee);

        // =========================
        // SEND MAIL
        // =========================

        payrollMailService.sendPayslip(
                payroll,
                employee.getEmail());

        // =========================
        // REDIRECT
        // =========================

        return "redirect:/user/financial";
    }

    // =========================
    // DOWNLOAD PAYSLIP PDF
    // =========================

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource>
    downloadPayslip(
            @PathVariable Long id) {

        Payroll payroll =
                payrollRepository
                .findById(id)
                .orElseThrow();

        Employee employee =
                employeeRepository
                .findById(
                        payroll.getEmployeeId())
                .orElseThrow();
        
        payrollMailService.sendPayslip(
                payroll,
                employee.getEmail());

        ByteArrayInputStream pdf =
                PayslipGenerator
                .generatePayslip(
                        payroll,
                        employee);

        HttpHeaders headers =
                new HttpHeaders();

        headers.add(
                "Content-Disposition",
                "inline; filename=payslip.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(
                        MediaType.APPLICATION_PDF)
                .body(
                        new InputStreamResource(pdf));
    }
}