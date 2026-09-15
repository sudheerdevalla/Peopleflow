package com.hr.hrapp.payroll.controller;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Leave;
import com.hr.hrapp.payroll.entity.Payroll;

import com.hr.hrapp.payroll.report.CEOReportService;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.payroll.service.PayrollMailService;
import com.hr.hrapp.payroll.service.PayrollService;
import com.hr.hrapp.payroll.util.PayslipGenerator;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.LeaveRepository;

@Controller
@RequestMapping("/payroll")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PayrollMailService payrollMailService;

    @Autowired
    private CEOReportService ceoReportService;
    
 // =========================
 // ADMIN PAYROLL PAGE
 // =========================
 @GetMapping
 @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
 public String payrollPage(
         @RequestParam(required = false) String month,
         Model model) {

     YearMonth payrollMonth = resolveMonth(month);

     String monthLabel =
             com.hr.hrapp.util.PayrollMonthUtil.format(payrollMonth);

     List<Employee> employees =
             employeeRepository.findByStatus("Active");

     List<Payroll> payrolls =
             payrollRepository.findByMonth(monthLabel);

     model.addAttribute("employees", employees);
     model.addAttribute("payrolls", payrolls);
     model.addAttribute("selectedMonth",
             payrollMonth.toString());
     
     LocalDate today = LocalDate.now();
     LocalDate monthEnd = payrollMonth.atEndOfMonth();

     boolean payrollMonthEnded = !today.isBefore(monthEnd);

     model.addAttribute("payrollMonthEnded", payrollMonthEnded);

     return "admin-payroll";
 }

    // =========================
    // GENERATE PAYROLL
    // =========================

 @GetMapping("/generate/{id}")
 @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
 public String generatePayroll(
         @PathVariable Long id,
         @RequestParam(required = false) String month) {

     Employee employee =
             employeeRepository
             .findById(id)
             .orElse(null);

     if (employee == null) {
         return "redirect:/payroll?error=EmployeeNotFound";
     }

     YearMonth selectedMonth = resolveMonth(month);

     LocalDate today = LocalDate.now();
     LocalDate monthEnd = selectedMonth.atEndOfMonth();

     // Payroll generation is allowed only at/after month end
     if (today.isBefore(monthEnd)) {
         return "redirect:/payroll?month=" + selectedMonth
                 + "&error=Payroll generation is available only after month end";
     }

     payrollService.calculateSalary(employee, selectedMonth);

     return "redirect:/payroll?month=" + selectedMonth;
 }
 @GetMapping("/finalize/{id}")
 @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
 public String finalizePayroll(
         @PathVariable Long id,
         @RequestParam(required = false) String month,
         Principal principal) {

     Employee employee =
             employeeRepository.findById(id).orElse(null);

     if (employee == null) {
         return "redirect:/payroll?error=EmployeeNotFound";
     }

     YearMonth selectedMonth = resolveMonth(month);

     try {

         Payroll payroll = payrollService.finalizePayroll(
                 id,
                 selectedMonth,
                 principal == null ? "system" : principal.getName()
         );

         payrollMailService.sendPayslip(
                 payroll,
                 employee.getEmail()
         );

         return "redirect:/payroll?month=" + selectedMonth;

     } catch (IllegalStateException ex) {

         return "redirect:/payroll?month=" + selectedMonth
                 + "&error=" + java.net.URLEncoder.encode(
                         ex.getMessage(),
                         java.nio.charset.StandardCharsets.UTF_8
                 );
     }
 }

    @GetMapping("/finalize-month")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    @ResponseBody
    public List<Payroll> finalizePayrollForMonth(@RequestParam String month,
                                                 Principal principal) {
        return payrollService.finalizePayrollForMonth(
                resolveMonth(month),
                principal == null ? "system" : principal.getName());
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

        if (payroll.getStatus() == null || !payroll.getStatus().equalsIgnoreCase("FINALIZED")) {
            throw new ResponseStatusException(BAD_REQUEST, "Payslips are available only for finalized payroll records");
        }
        

        Employee employee =
                employeeRepository
                .findById(payroll.getEmployeeId())
                .orElseThrow();
        YearMonth payrollMonth;

        try {
            payrollMonth = YearMonth.parse(
                    payroll.getMonth(),
                    new java.time.format.DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("MMMM yyyy")
                            .toFormatter(java.util.Locale.ENGLISH));
        } catch (java.time.format.DateTimeParseException e) {
            payrollMonth = YearMonth.parse(
                    payroll.getMonth(),
                    new java.time.format.DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern("MMM yyyy")
                            .toFormatter(java.util.Locale.ENGLISH));
        }
        YearMonth currentMonth = YearMonth.now();

        if (payrollMonth.isAfter(currentMonth)) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Payslip download is not allowed for future months");
        }

        LocalDate leaveStartDate = payrollMonth.atDay(1);
        LocalDate leaveEndDate = payrollMonth.atEndOfMonth();

        List<Leave> approvedLeaves =
                leaveRepository.findByEmpIdAndDateBetweenAndStatus(
                        employee.getEmpId(),
                        leaveStartDate,
                        leaveEndDate,
                        "APPROVED");

        long sickLeaveCount = approvedLeaves.stream()
                .filter(leave -> leave.getType() != null
                        && leave.getType().equalsIgnoreCase("SICK"))
                .count();

        long annualLeaveCount = approvedLeaves.stream()
                .filter(leave -> leave.getType() != null
                        && leave.getType().equalsIgnoreCase("ANNUAL"))
                .count();

        ByteArrayInputStream pdf =
                PayslipGenerator.generatePayslip(
                        payroll,
                        employee,
                        sickLeaveCount,
                        annualLeaveCount);

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

    @GetMapping("/consolidated/download")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public ResponseEntity<InputStreamResource> downloadConsolidatedSalarySheet(@RequestParam String month) {
    	YearMonth selectedMonth = resolveMonth(month);

    	LocalDate today = LocalDate.now();

    	LocalDate monthEnd = selectedMonth.atEndOfMonth();

    	if (today.isBefore(monthEnd)) {
    	    throw new ResponseStatusException(
    	            BAD_REQUEST,
    	            "Consolidated memo will be ready for download at month end");
    	}

    	ByteArrayInputStream report =
    	        ceoReportService.generateConsolidatedSalarySheet(selectedMonth);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=consolidated-salary-sheet.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(report));
    }

    private YearMonth resolveMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Month must be in yyyy-MM format");
        }
    }
}