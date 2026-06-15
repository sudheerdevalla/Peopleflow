package com.hr.hrapp.controller;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Salary;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.SalaryRepository;
import com.hr.hrapp.service.EmailService;
import com.hr.hrapp.service.EmployeeService;
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
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private PayrollRepository payrollRepository;

    // ================== MAIN PAGE ==================
    @GetMapping("/financial")
    public String financial(@RequestParam(required = false)
                            String month,

                            Model model,

                            Principal principal) {

        // =========================
        // LOGIN CHECK
        // =========================

        if(principal == null) {

            return "redirect:/login";
        }

        String username =
                principal.getName();

        Employee emp =
                employeeRepository
                .findByEmail(username);

        // =========================
        // EMPLOYEE DETAILS
        // =========================

        model.addAttribute("employee", emp);

        // =========================
        // OLD SALARY HISTORY
        // =========================

        List<Salary> salaries =
                salaryRepository
                .findByEmployeeId(emp.getEmpId());

        model.addAttribute(
                "salaryList",
                salaries);

        // =========================
        // PAYROLL HISTORY
        // =========================

        List<Payroll> payrollHistory =
                payrollRepository
                .findByEmployeeIdOrderByIdDesc(
                        emp.getEmpId());

        model.addAttribute(
                "payrollHistory",
                payrollHistory);

        // =========================
        // CURRENT MONTH SALARY
        // =========================

        LocalDate now = LocalDate.now();

        int monthValue =
                now.getMonthValue();

        int year =
                now.getYear();

        double calculatedSalary =
                financialService
                .calculateSalary(
                        emp,
                        monthValue,
                        year);

        // =========================
        // LATEST PAYROLL FETCH
        // =========================

        Payroll payroll =
                payrollRepository
                .findTopByEmployeeIdOrderByIdDesc(
                        emp.getEmpId());
     // =========================
     // DEBUG
     // =========================

     System.out.println("EMP ID = " + emp.getEmpId());

     if(payroll != null){

         System.out.println("PAYROLL ID = " + payroll.getId());
         System.out.println("PAYROLL EMP ID = " + payroll.getEmployeeId());
         System.out.println("PAYROLL NET = " + payroll.getNetSalary());
         System.out.println("PAYROLL MONTH = " + payroll.getMonth());

     }else{

         System.out.println("PAYROLL IS NULL");
     }

        if(payroll != null) {

            model.addAttribute(
                    "calculatedSalary",
                    payroll.getNetSalary());

        } else {

            model.addAttribute(
                    "calculatedSalary",
                    calculatedSalary);
        }

        return "financial";
    }
    @PostMapping("/financial/save")
    public String saveFinancialDetails(
            @ModelAttribute Employee updatedEmployee,
            Principal principal) {

        employeeService.updateFinancialDetails(
                principal.getName(),
                updatedEmployee);

        return "redirect:/user/financial";
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