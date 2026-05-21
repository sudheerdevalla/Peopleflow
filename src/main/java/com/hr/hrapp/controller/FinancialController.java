package com.hr.hrapp.controller;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Salary;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.SalaryRepository;
import com.hr.hrapp.service.EmailService;
import com.hr.hrapp.service.FinancialService;
import com.hr.hrapp.service.PdfGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/user")
public class FinancialController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired 
    private FinancialService financialService;

    // ================== MAIN PAGE ==================
    @GetMapping("/financial")
    public String financial(@RequestParam(required = false) String month,
                            Model model,
                            Principal principal) {

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        // Employee details
        model.addAttribute("employee", emp);

        // Salary history 
        List<Salary> salaries = salaryRepository.findByEmployeeId(emp.getEmpId());
        model.addAttribute("salaryList", salaries);

        // 🔥 Timesheet based salary calculation
        LocalDate now = LocalDate.now();
        int monthValue = now.getMonthValue();
        int year = now.getYear();

        double calculatedSalary = financialService.calculateSalary(emp, monthValue, year);

        model.addAttribute("calculatedSalary", calculatedSalary);

        return "financial";
    }

    // ================== VIEW ==================
    @GetMapping("/view")
    public String viewSalary(@RequestParam String month,
                             Model model,
                             Principal principal) {

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        List<Salary> salaries = salaryRepository.findByEmployeeId(emp.getEmpId());
        model.addAttribute("salaryList", salaries);

        model.addAttribute("employee", emp);

        String formattedMonth = convertMonth(month);
        Salary salary = salaryRepository.findByMonth(formattedMonth);
        model.addAttribute("salary", salary);

        return "financial";
    }

    // ================== DOWNLOAD ==================
    @GetMapping("/download")
    public void downloadSalary(@RequestParam String month,
                               HttpServletResponse response) throws Exception {

        String formattedMonth = convertMonth(month);
        Salary salary = salaryRepository.findByMonth(formattedMonth);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=payslip.pdf");

        PdfGenerator.generate(response, salary);
    }

    // ================== SEND MAIL ==================
    @PostMapping("/send-mail")
    public String sendMail(@RequestParam String month,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {

        String formattedMonth = convertMonth(month);
        Salary salary = salaryRepository.findByMonth(formattedMonth);

        emailService.sendSalaryMail(principal.getName(), salary);

        redirectAttributes.addFlashAttribute("message", "Mail sent successfully!");
        return "redirect:/user/financial?month=" + month;
    }

    // ================== MONTH CONVERTER ==================
    private String convertMonth(String month) {
        YearMonth ym = YearMonth.parse(month);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        return ym.format(formatter);
    }
}