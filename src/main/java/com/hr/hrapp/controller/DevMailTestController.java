package com.hr.hrapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Leave;
import com.hr.hrapp.entity.TravelRequest;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.report.CEOReportService;
import com.hr.hrapp.payroll.service.PayrollMailService;
import com.hr.hrapp.payroll.service.PayrollService;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.LeaveRepository;
import com.hr.hrapp.repository.TravelRequestRepository;
import com.hr.hrapp.scheduler.PayrollScheduler;
import com.hr.hrapp.scheduler.TimesheetReminderScheduler;
import com.hr.hrapp.service.EmailService;

import java.util.*;

/**
 * Development-only REST controller for testing email workflows.
 * 
 * This controller is ONLY enabled when Spring profile is "dev".
 * All endpoints are for testing purposes and should NOT be used in production.
 * 
 * To enable:
 *   1. Set Spring profile to "dev" in application.properties or environment
 *   2. Access endpoints at http://localhost:8443/dev/*
 */
@Controller
@RequestMapping("/dev")
@Profile("dev")
public class DevMailTestController {

    private static final Logger logger = LoggerFactory.getLogger(DevMailTestController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private PayrollMailService payrollMailService;

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private CEOReportService ceoReportService;

    @Autowired
    private TimesheetReminderScheduler timesheetReminderScheduler;

    @Autowired
    private PayrollScheduler payrollScheduler;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private TravelRequestRepository travelRequestRepository;

    // ===========================
    // TEST WELCOME MAIL
    // ===========================
    @GetMapping("/test-welcome-mail")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testWelcomeMail(
            @RequestParam(required = false, defaultValue = "sudheerdevalla950214@gmail.com") String email) {

        String status = "FAILED";
        String message = "";

        try {
            logger.info("Testing Welcome Mail to: {}", email);

            String body = "<p>Dear Employee,</p>"
                    + "<p>Welcome to Renwion Clean Enviro Solutions Private Limited. Your account has been created.</p>"
                    + "<p>Regards,<br/>HR Team</p>";

            emailService.sendMail(email, "Welcome to Renwion Clean Enviro Solutions", body);

            status = "SUCCESS";
            message = "Welcome email sent to: " + email;
            logger.info("✓ Welcome Mail: {}", message);

        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            logger.error("✗ Welcome Mail Failed: {}", message, e);
        }

        return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-welcome-mail", "message", message));
    }

    // ===========================
    // TEST FORGOT PASSWORD
    // ===========================
    @GetMapping("/test-forgot-password")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testForgotPassword(
            @RequestParam(required = false, defaultValue = "sudheerdevalla950214@gmail.com") String email) {

        String status = "FAILED";
        String message = "";

        try {
            logger.info("Testing Forgot Password for: {}", email);

            // Generate OTP
            String otp = String.valueOf(100000 + new Random().nextInt(900000));

            // Send OTP email using MimeMessageHelper with setFrom
            emailService.sendMail(
                    email,
                    "PeopleFlow Password Reset OTP",
                    "Your OTP is: " + otp
            );

            status = "SUCCESS";
            message = "Forgot password email sent with OTP: " + otp;
            logger.info("✓ Forgot Password: {}", message);

        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            logger.error("✗ Forgot Password Failed: {}", message, e);
        }

        return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-forgot-password", "message", message));
    }

    // ===========================
    // TEST PAYSLIP
    // ===========================
    @GetMapping("/test-payslip")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testPayslip(
            @RequestParam(required = false) Long employeeId) {

        String status = "FAILED";
        String message = "";

        try {
            logger.info("Testing Payslip generation and email");

            // Get first employee if not specified
            Long empId = employeeId;
            if (empId == null) {
                Employee emp = employeeRepository.findAll().stream().findFirst().orElse(null);
                if (emp == null) {
                    status = "SKIPPED";
                    message = "No employees found in database. Skipping payslip test.";
                    logger.info("⊘ Payslip: {}", message);
                    return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-payslip", "message", message));
                }
                empId = emp.getEmpId();
            }

            final Long finalEmpId = empId;
            Employee employee = employeeRepository.findById(finalEmpId).orElse(null);
            
            if (employee == null) {
                status = "SKIPPED";
                message = "Employee not found with ID: " + finalEmpId + ". Skipping payslip test.";
                logger.info("⊘ Payslip: {}", message);
                return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-payslip", "message", message));
            }

            // Validate employee has basic salary set
            if (employee.getBasicSalary() <= 0) {
                status = "SKIPPED";
                message = "Employee " + employee.getName() + " has no basic salary set. Skipping payslip test.";
                logger.info("⊘ Payslip: {}", message);
                return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-payslip", "message", message));
            }

            // Calculate and generate payroll with safe null defaults
            Payroll payroll = payrollService.calculateSalary(employee);
            
            // Validate payroll calculated
            if (payroll == null) {
                status = "SKIPPED";
                message = "Could not calculate payroll for employee " + employee.getName();
                logger.info("⊘ Payslip: {}", message);
                return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-payslip", "message", message));
            }

            // Send payslip via PayrollMailService (with password-protected PDF)
            payrollMailService.sendPayslip(payroll, employee.getEmail());

            status = "SUCCESS";
            message = "Payslip generated and sent to: " + employee.getEmail();
            logger.info("✓ Payslip: {}", message);

        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            logger.error("✗ Payslip Failed: {}", message, e);
        }

        return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-payslip", "message", message));
    }

    // ===========================
    // TEST CEO REPORT
    // ===========================
    @GetMapping("/test-ceo-report")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testCeoReport() {

        String status = "FAILED";
        String message = "";

        try {
            logger.info("Testing CEO Report generation and email");

            ceoReportService.sendCEOReport();

            status = "SUCCESS";
            message = "CEO consolidated payroll report sent";
            logger.info("✓ CEO Report: {}", message);

        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            logger.error("✗ CEO Report Failed: {}", message, e);
        }

        return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-ceo-report", "message", message));
    }

    // ===========================
    // TEST LEAVE APPROVAL
    // ===========================
    @GetMapping("/test-leave-approval")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testLeaveApproval() {

        String status = "FAILED";
        String message = "";

        try {
            logger.info("Testing Leave Approval email");

            // Get first leave with PENDING status safely
            Leave leave = leaveRepository.findAll().stream()
                    .filter(l -> "PENDING".equals(l.getStatus()))
                    .findFirst()
                    .orElse(null);
            
            if (leave == null) {
                status = "SKIPPED";
                message = "No pending leave requests found in database. Skipping leave approval test.";
                logger.info("⊘ Leave Approval: {}", message);
                return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-leave-approval", "message", message));
            }

            Employee emp = employeeRepository.findById(leave.getEmpId()).orElse(null);
            
            if (emp == null) {
                status = "SKIPPED";
                message = "Employee not found for leave ID: " + leave.getEmpId() + ". Skipping test.";
                logger.info("⊘ Leave Approval: {}", message);
                return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-leave-approval", "message", message));
            }

            // Build approval email
            String body = "<html><body style='font-family:Arial;background:#f4f4f4;padding:20px;'>"
                    + "<div style='max-width:600px;margin:auto;background:white;border-radius:10px;'>"
                    + "<div style='padding:30px;'>"
                    + "<h2 style='color:green;'>Leave Approved ✅</h2>"
                    + "<p>Hello <b>" + emp.getName() + "</b>,</p>"
                    + "<p>Your leave request has been approved.</p>"
                    + "<h3>Leave Details</h3>"
                    + "<table border='1' cellpadding='10'>"
                    + "<tr><td><b>Leave Type</b></td><td>" + leave.getType() + "</td></tr>"
                    + "<tr><td><b>Leave Date</b></td><td>" + leave.getDate() + "</td></tr>"
                    + "</table>"
                    + "<p style='color:gray;font-size:13px;'>This is an auto-generated email.</p>"
                    + "</div></div></body></html>";

            emailService.sendMail(emp.getEmail(), "Leave Approved", body);

            status = "SUCCESS";
            message = "Leave approval email sent to: " + emp.getEmail();
            logger.info("✓ Leave Approval: {}", message);

        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            logger.error("✗ Leave Approval Failed: {}", message, e);
        }

        return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-leave-approval", "message", message));
    }

    // ===========================
    // TEST LEAVE REJECTION
    // ===========================
    @GetMapping("/test-leave-rejection")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testLeaveRejection() {

        String status = "FAILED";
        String message = "";

        try {
            logger.info("Testing Leave Rejection email");

            // Get first leave with PENDING status safely
            Leave leave = leaveRepository.findAll().stream()
                    .filter(l -> "PENDING".equals(l.getStatus()))
                    .findFirst()
                    .orElse(null);
            
            if (leave == null) {
                status = "SKIPPED";
                message = "No pending leave requests found in database. Skipping leave rejection test.";
                logger.info("⊘ Leave Rejection: {}", message);
                return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-leave-rejection", "message", message));
            }

            Employee emp = employeeRepository.findById(leave.getEmpId()).orElse(null);
            
            if (emp == null) {
                status = "SKIPPED";
                message = "Employee not found for leave ID: " + leave.getEmpId() + ". Skipping test.";
                logger.info("⊘ Leave Rejection: {}", message);
                return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-leave-rejection", "message", message));
            }

            // Build rejection email
            String body = "<html><body style='font-family:Arial;background:#f4f4f4;padding:20px;'>"
                    + "<div style='max-width:600px;margin:auto;background:white;border-radius:10px;'>"
                    + "<div style='padding:30px;'>"
                    + "<h2 style='color:red;'>Leave Rejected ❌</h2>"
                    + "<p>Hello <b>" + emp.getName() + "</b>,</p>"
                    + "<p>Your leave request has been rejected.</p>"
                    + "<h3>Leave Details</h3>"
                    + "<table border='1' cellpadding='10'>"
                    + "<tr><td><b>Leave Type</b></td><td>" + leave.getType() + "</td></tr>"
                    + "<tr><td><b>Leave Date</b></td><td>" + leave.getDate() + "</td></tr>"
                    + "</table>"
                    + "<p style='color:gray;font-size:13px;'>This is an auto-generated email.</p>"
                    + "</div></div></body></html>";

            emailService.sendMail(emp.getEmail(), "Leave Rejected", body);

            status = "SUCCESS";
            message = "Leave rejection email sent to: " + emp.getEmail();
            logger.info("✓ Leave Rejection: {}", message);

        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            logger.error("✗ Leave Rejection Failed: {}", message, e);
        }

        return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-leave-rejection", "message", message));
    }

    // ===========================
    // TEST TIMESHEET REMINDER
    // ===========================
    @GetMapping("/test-timesheet-reminder")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testTimesheetReminder() {

        String status = "FAILED";
        String message = "";

        try {
            logger.info("Testing Timesheet Reminder Scheduler");

            timesheetReminderScheduler.checkMissingTimesheets();

            status = "SUCCESS";
            message = "Timesheet reminder scheduler executed";
            logger.info("✓ Timesheet Reminder: {}", message);

        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            logger.error("✗ Timesheet Reminder Failed: {}", message, e);
        }

        return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-timesheet-reminder", "message", message));
    }

    // ===========================
    // TEST PAYROLL SCHEDULER
    // ===========================
    @GetMapping("/test-payroll-scheduler")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testPayrollScheduler() {

        String status = "FAILED";
        String message = "";

        try {
            logger.info("Testing Payroll Scheduler");

            payrollScheduler.autoGeneratePayroll();

            status = "SUCCESS";
            message = "Payroll scheduler executed (payslips sent to all employees)";
            logger.info("✓ Payroll Scheduler: {}", message);

        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            logger.error("✗ Payroll Scheduler Failed: {}", message, e);
        }

        return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-payroll-scheduler", "message", message));
    }

    // ===========================
    // TEST TRAVEL MAIL
    // ===========================
    @GetMapping("/test-travel-mail")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testTravelMail() {

        String status = "FAILED";
        String message = "";

        try {
            logger.info("Testing Travel Request Notification email");

            // Get first traveler with REQUESTED status safely
            TravelRequest travelRequest = travelRequestRepository.findAll().stream()
                    .filter(t -> "REQUESTED".equals(t.getStatus()))
                    .findFirst()
                    .orElse(null);

            if (travelRequest == null) {
                status = "SKIPPED";
                message = "No travel requests with REQUESTED status found. Skipping travel mail test.";
                logger.info("⊘ Travel Mail: {}", message);
                return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-travel-mail", "message", message));
            }

            Employee traveler = employeeRepository.findById(travelRequest.getEmpId()).orElse(null);

            if (traveler == null) {
                status = "SKIPPED";
                message = "Employee not found for travel request ID: " + travelRequest.getEmpId() + ". Skipping test.";
                logger.info("⊘ Travel Mail: {}", message);
                return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-travel-mail", "message", message));
            }

            // Get manager
            Employee manager = traveler.getManager();
            if (manager == null) {
                status = "SKIPPED";
                message = "Travel requester (" + traveler.getName() + ") has no manager assigned. Skipping travel mail test.";
                logger.info("⊘ Travel Mail: {}", message);
                return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-travel-mail", "message", message));
            }

            // Send travel request notification
            String managerBody = "<h3>New Travel Request</h3>"
                    + "<p>Employee: " + traveler.getName() + "</p>"
                    + "<p>Destination: " + travelRequest.getDestination() + "</p>"
                    + "<p>From: " + travelRequest.getFromDate() + " To: " + travelRequest.getToDate() + "</p>";

            emailService.sendMail(manager.getEmail(), "New Travel Request", managerBody);

            status = "SUCCESS";
            message = "Travel notification email sent to manager: " + manager.getEmail();
            logger.info("✓ Travel Mail: {}", message);

        } catch (Exception e) {
            message = "Error: " + e.getMessage();
            logger.error("✗ Travel Mail Failed: {}", message, e);
        }

        return ResponseEntity.ok(Map.of("status", status, "endpoint", "test-travel-mail", "message", message));
    }

    // ===========================
    // TEST ALL MAILS
    // ===========================
    @GetMapping("/test-all-mails")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testAllMails() {

        logger.info("====== STARTING COMPREHENSIVE EMAIL TEST ======");

        List<Map<String, String>> results = new ArrayList<>();
        int passed = 0;
        int failed = 0;
        int skipped = 0;

        // Test 1: Welcome Mail
        try {
            logger.info("\n1. Testing Welcome Mail...");
            emailService.sendMail("sudheerdevalla950214@gmail.com", "Welcome to Renwion Clean Enviro Solutions",
                    "<p>Welcome Test Email</p>");
            results.add(Map.of("endpoint", "test-welcome-mail", "status", "✔ Success", "message", ""));
            passed++;
        } catch (Exception e) {
            results.add(Map.of("endpoint", "test-welcome-mail", "status", "✖ Failed", "message", e.getMessage()));
            failed++;
        }

        // Test 2: Forgot Password
        try {
            logger.info("\n2. Testing Forgot Password...");
            String otp = String.valueOf(100000 + new Random().nextInt(900000));
            emailService.sendMail("sudheerdevalla950214@gmail.com", "PeopleFlow Password Reset OTP",
                    "Your OTP is: " + otp);
            results.add(Map.of("endpoint", "test-forgot-password", "status", "✔ Success", "message", ""));
            passed++;
        } catch (Exception e) {
            results.add(Map.of("endpoint", "test-forgot-password", "status", "✖ Failed", "message", e.getMessage()));
            failed++;
        }

        // Test 3: Payslip
        try {
            logger.info("\n3. Testing Payslip...");
            Employee emp = employeeRepository.findAll().stream()
                    .filter(e -> e.getBasicSalary() > 0)
                    .findFirst()
                    .orElse(null);
            
            if (emp == null) {
                results.add(Map.of("endpoint", "test-payslip", "status", "⊘ Skipped", "message", "No employees with salary found"));
                skipped++;
            } else {
                Payroll payroll = payrollService.calculateSalary(emp);
                if (payroll != null) {
                    payrollMailService.sendPayslip(payroll, emp.getEmail());
                    results.add(Map.of("endpoint", "test-payslip", "status", "✔ Success", "message", ""));
                    passed++;
                } else {
                    results.add(Map.of("endpoint", "test-payslip", "status", "⊘ Skipped", "message", "Could not calculate payroll"));
                    skipped++;
                }
            }
        } catch (Exception e) {
            results.add(Map.of("endpoint", "test-payslip", "status", "✖ Failed", "message", e.getMessage()));
            failed++;
        }

        // Test 4: CEO Report
        try {
            logger.info("\n4. Testing CEO Report...");
            ceoReportService.sendCEOReport();
            results.add(Map.of("endpoint", "test-ceo-report", "status", "✔ Success", "message", ""));
            passed++;
        } catch (Exception e) {
            results.add(Map.of("endpoint", "test-ceo-report", "status", "✖ Failed", "message", e.getMessage()));
            failed++;
        }

        // Test 5: Leave Approval
        try {
            logger.info("\n5. Testing Leave Approval...");
            Leave leave = leaveRepository.findAll().stream()
                    .filter(l -> "PENDING".equals(l.getStatus()))
                    .findFirst()
                    .orElse(null);
            
            if (leave == null) {
                results.add(Map.of("endpoint", "test-leave-approval", "status", "⊘ Skipped", "message", "No pending leaves found"));
                skipped++;
            } else {
                Employee emp = employeeRepository.findById(leave.getEmpId()).orElse(null);
                if (emp != null) {
                    String body = "<h2 style='color:green;'>Leave Approved ✅</h2><p>Hello " + emp.getName()
                            + ",</p><p>Your leave request has been approved.</p>";
                    emailService.sendMail(emp.getEmail(), "Leave Approved", body);
                    results.add(Map.of("endpoint", "test-leave-approval", "status", "✔ Success", "message", ""));
                    passed++;
                } else {
                    results.add(Map.of("endpoint", "test-leave-approval", "status", "⊘ Skipped", "message", "Employee not found"));
                    skipped++;
                }
            }
        } catch (Exception e) {
            results.add(Map.of("endpoint", "test-leave-approval", "status", "✖ Failed", "message", e.getMessage()));
            failed++;
        }

        // Test 6: Leave Rejection
        try {
            logger.info("\n6. Testing Leave Rejection...");
            Leave leave = leaveRepository.findAll().stream()
                    .filter(l -> "PENDING".equals(l.getStatus()))
                    .findFirst()
                    .orElse(null);
            
            if (leave == null) {
                results.add(Map.of("endpoint", "test-leave-rejection", "status", "⊘ Skipped", "message", "No pending leaves found"));
                skipped++;
            } else {
                Employee emp = employeeRepository.findById(leave.getEmpId()).orElse(null);
                if (emp != null) {
                    String body = "<h2 style='color:red;'>Leave Rejected ❌</h2><p>Hello " + emp.getName()
                            + ",</p><p>Your leave request has been rejected.</p>";
                    emailService.sendMail(emp.getEmail(), "Leave Rejected", body);
                    results.add(Map.of("endpoint", "test-leave-rejection", "status", "✔ Success", "message", ""));
                    passed++;
                } else {
                    results.add(Map.of("endpoint", "test-leave-rejection", "status", "⊘ Skipped", "message", "Employee not found"));
                    skipped++;
                }
            }
        } catch (Exception e) {
            results.add(Map.of("endpoint", "test-leave-rejection", "status", "✖ Failed", "message", e.getMessage()));
            failed++;
        }

        // Test 7: Timesheet Reminder
        try {
            logger.info("\n7. Testing Timesheet Reminder...");
            timesheetReminderScheduler.checkMissingTimesheets();
            results.add(Map.of("endpoint", "test-timesheet-reminder", "status", "✔ Success", "message", ""));
            passed++;
        } catch (Exception e) {
            results.add(Map.of("endpoint", "test-timesheet-reminder", "status", "✖ Failed", "message", e.getMessage()));
            failed++;
        }

        // Test 8: Payroll Scheduler
        try {
            logger.info("\n8. Testing Payroll Scheduler...");
            payrollScheduler.autoGeneratePayroll();
            results.add(Map.of("endpoint", "test-payroll-scheduler", "status", "✔ Success", "message", ""));
            passed++;
        } catch (Exception e) {
            results.add(Map.of("endpoint", "test-payroll-scheduler", "status", "✖ Failed", "message", e.getMessage()));
            failed++;
        }

        // Test 9: Travel Mail
        try {
            logger.info("\n9. Testing Travel Mail...");
            TravelRequest travelRequest = travelRequestRepository.findAll().stream()
                    .filter(t -> "REQUESTED".equals(t.getStatus()))
                    .findFirst()
                    .orElse(null);
            
            if (travelRequest == null) {
                results.add(Map.of("endpoint", "test-travel-mail", "status", "⊘ Skipped", "message", "No travel requests found"));
                skipped++;
            } else {
                Employee traveler = employeeRepository.findById(travelRequest.getEmpId()).orElse(null);
                if (traveler != null && traveler.getManager() != null) {
                    String body = "<h3>New Travel Request</h3><p>Employee: " + traveler.getName() + "</p>";
                    emailService.sendMail(traveler.getManager().getEmail(), "New Travel Request", body);
                    results.add(Map.of("endpoint", "test-travel-mail", "status", "✔ Success", "message", ""));
                    passed++;
                } else {
                    results.add(Map.of("endpoint", "test-travel-mail", "status", "⊘ Skipped", "message", "No manager assigned"));
                    skipped++;
                }
            }
        } catch (Exception e) {
            results.add(Map.of("endpoint", "test-travel-mail", "status", "✖ Failed", "message", e.getMessage()));
            failed++;
        }

        logger.info("\n====== EMAIL TEST SUMMARY ======");
        logger.info("Total Tests  : {}", passed + failed + skipped);
        logger.info("Passed       : {}", passed);
        logger.info("Failed       : {}", failed);
        logger.info("Skipped      : {}", skipped);
        logger.info("=====================================\n");

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("summary", Map.of("total", passed + failed + skipped, "passed", passed, "failed", failed, "skipped", skipped));
        summary.put("results", results);

        return ResponseEntity.ok(summary);
    }

}
