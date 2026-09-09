package com.hr.hrapp.controller;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Salary;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.payroll.service.PayrollMailService;
import com.hr.hrapp.payroll.util.PayslipGenerator;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.LeaveRepository;
import com.hr.hrapp.repository.SalaryRepository;
import com.hr.hrapp.service.AuditTrailService;
import com.hr.hrapp.service.EmailService;
import com.hr.hrapp.service.EmployeeService;
import com.hr.hrapp.service.FinancialAccessOtpService;
import com.hr.hrapp.service.FinancialService;
import com.hr.hrapp.service.PdfGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.hr.hrapp.util.PayrollMonthUtil;

@Controller
@RequestMapping("/user")
public class FinancialController {

    private static final String FINANCIAL_ACCESS_VERIFIED_AT = "financialAccessVerifiedAt";

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
    
    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private FinancialAccessOtpService financialAccessOtpService;

    @Autowired
    private PayrollMailService payrollMailService;

    @Autowired
    private AuditTrailService auditTrailService;

	//@Autowired
	//private EmailService emailService;

    // ================== MAIN PAGE ==================
    @GetMapping("/financial")
    public String financial(@RequestParam(required = false)
                            String month,

                            Model model,

                            Principal principal,
                            HttpSession session) {

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

        model.addAttribute("employee", emp);
        model.addAttribute("otpRequired", false);

        if (!isFinancialAccessVerified(session)) {
            model.addAttribute("otpRequired", true);
            model.addAttribute("maskedEmail", financialAccessOtpService.maskEmail(emp.getEmail()));
            return "financial";
        }

        // =========================
        // EMPLOYEE DETAILS
        // =========================

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
                .findByEmployeeIdAndStatusOrderByIdDesc(
                        emp.getEmpId(),
                        "FINALIZED");

        model.addAttribute(
                "payrollHistory",
                payrollHistory);

        // =========================
        // CURRENT MONTH SALARY
        // =========================

        Payroll selectedPayroll = resolveRequestedPayroll(emp, month, payrollHistory);

        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedPayroll", selectedPayroll);
        model.addAttribute(
                "calculatedSalary",
                selectedPayroll != null ? selectedPayroll.getNetSalary() : 0.0);

        return "financial";
    }
    @PostMapping("/financial/save")
    public String saveFinancialDetails(
            @ModelAttribute Employee updatedEmployee,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        auditTrailService.record(
                principal == null ? "anonymous" : principal.getName(),
                "EMPLOYEE_FINANCIAL_EDIT_BLOCKED",
                "/user/financial/save",
                "SUCCESS",
                "EMPLOYEE",
                principal == null ? null : principal.getName(),
                "Employee self-service editing of payroll-sensitive fields is blocked; HR/Admin must update employee master.");
        redirectAttributes.addFlashAttribute("error", "Financial and payroll-sensitive fields are maintained by HR/Admin. Please contact HR for changes.");
        return "redirect:/user/financial";
    }

    @PostMapping("/financial/request-otp")
    public String requestFinancialOtp(Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            Employee emp = employeeRepository.findByEmail(principal.getName());
            financialAccessOtpService.issueOtp(principal.getName(), emp.getEmail());
            redirectAttributes.addFlashAttribute("message", "OTP sent to your registered email.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/user/financial";
    }

    @PostMapping("/financial/verify-otp")
    public String verifyFinancialOtp(@RequestParam String otp,
                                     Principal principal,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        try {
            financialAccessOtpService.verifyOtp(principal.getName(), otp);
            session.setAttribute(FINANCIAL_ACCESS_VERIFIED_AT, LocalDateTime.now());
            redirectAttributes.addFlashAttribute("message", "Financial access verified successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/user/financial";
    }

    // ================== VIEW ==================
    @GetMapping("/view")
    public String viewSalary(@RequestParam String month,
                             Model model,
                             Principal principal,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        if (!isFinancialAccessVerified(session)) {
            redirectAttributes.addFlashAttribute("error", "Please verify OTP before accessing financial records.");
            return "redirect:/user/financial";
        }

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        List<Salary> salaries = salaryRepository.findByEmployeeId(emp.getEmpId());
        model.addAttribute("salaryList", salaries);

        model.addAttribute("employee", emp);

        List<Payroll> payrollHistory = payrollRepository.findByEmployeeIdAndStatusOrderByIdDesc(emp.getEmpId(), "FINALIZED");
        Payroll selectedPayroll = resolveRequestedPayroll(emp, month, payrollHistory);
        model.addAttribute("payrollHistory", payrollHistory);
        model.addAttribute("selectedPayroll", selectedPayroll);
        model.addAttribute("calculatedSalary", selectedPayroll != null ? selectedPayroll.getNetSalary() : 0.0);

        return "financial";
    }

    // ================== DOWNLOAD ==================
    @GetMapping("/download")
    public void downloadSalary(@RequestParam String month,
                               HttpServletResponse response,
                               Principal principal,
                               HttpSession session) throws Exception {

        enforceFinancialAccess(session);
        Employee employee = employeeRepository.findByEmail(principal.getName());
        Payroll payroll = resolveRequestedPayroll(employee, month,
                payrollRepository.findByEmployeeIdAndStatusOrderByIdDesc(employee.getEmpId(), "FINALIZED"));
        if (payroll == null) {
            throw new IllegalStateException("No finalized payroll found for requested month");
        }
        YearMonth payrollMonth = YearMonth.parse(month);

        LocalDate leaveStartDate = payrollMonth.atDay(1);
        LocalDate leaveEndDate = payrollMonth.atEndOfMonth();

        List<com.hr.hrapp.entity.Leave> approvedLeaves =
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
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=payslip.pdf");

        ByteArrayInputStream pdf =
                PayslipGenerator.generatePayslip(
                        payroll,
                        employee,
                        sickLeaveCount,
                        annualLeaveCount);
        response.getOutputStream().write(pdf.readAllBytes());
        response.flushBuffer();
    }

    // ================== SEND MAIL ==================
    @PostMapping("/send-mail")
    public String sendMail(@RequestParam String month,
                           Principal principal,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        try {
            enforceFinancialAccess(session);
            Employee employee = employeeRepository.findByEmail(principal.getName());
            Payroll payroll = resolveRequestedPayroll(employee, month,
                    payrollRepository.findByEmployeeIdAndStatusOrderByIdDesc(employee.getEmpId(), "FINALIZED"));
            if (payroll == null) {
                redirectAttributes.addFlashAttribute("error", "No finalized payroll found for requested month.");
                return "redirect:/user/financial?month=" + month;
            }
            payrollMailService.sendPayslip(payroll, principal.getName());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/user/financial?month=" + month;
        }

        redirectAttributes.addFlashAttribute("message", "Mail sent successfully!");
        return "redirect:/user/financial?month=" + month;
    }

    // ================== MONTH CONVERTER ==================
    private String convertMonth(String month) {
        YearMonth ym = YearMonth.parse(month);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        return ym.format(formatter);
    }

    private Payroll resolveRequestedPayroll(Employee employee, String month, List<Payroll> payrollHistory) {
        if (employee == null) {
            return null;
        }

        if (month != null && !month.isBlank()) {
            String formattedMonth = PayrollMonthUtil.format(YearMonth.parse(month));
            return payrollHistory.stream()
                    .filter(payroll -> payroll.getMonth() != null && payroll.getMonth().equalsIgnoreCase(formattedMonth))
                    .findFirst()
                    .orElse(null);
        }

        Payroll latestFinalized = financialService.getLatestFinalizedPayroll(employee.getEmpId());
        if (latestFinalized != null) {
            return latestFinalized;
        }

        return payrollHistory.stream().findFirst().orElse(null);
    }

    private boolean isFinancialAccessVerified(HttpSession session) {
        Object verifiedAt = session.getAttribute(FINANCIAL_ACCESS_VERIFIED_AT);
        if (!(verifiedAt instanceof LocalDateTime verifiedTime)) {
            return false;
        }
        return verifiedTime.plusMinutes(10).isAfter(LocalDateTime.now());
    }

    private void enforceFinancialAccess(HttpSession session) {
        if (!isFinancialAccessVerified(session)) {
            throw new IllegalStateException("Please verify OTP before accessing financial records.");
        }
    }
}