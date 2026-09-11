package com.hr.hrapp.payroll.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.YearMonth;

import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Leave;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.util.PayslipGenerator;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.LeaveRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class PayrollMailService {

    private static final Logger logger = LoggerFactory.getLogger(PayrollMailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private LeaveRepository leaveRepository;

    @Value("${mail.from:connect@renwion.in}")
    private String mailFrom;

    public void sendPayslip(
            Payroll payroll,
            String employeeEmail) {

        try {

            if (payroll == null || payroll.getStatus() == null || !payroll.getStatus().equalsIgnoreCase("FINALIZED")) {
                throw new IllegalStateException("Payslips can only be sent for finalized payroll records");
            }

            logger.info("PayrollMailService.sendPayslip started for email={}", employeeEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(mailFrom);
            helper.setTo(employeeEmail);
            helper.setSubject("Renwion Payslip - " + payroll.getMonth());
            helper.setText(
                    "Dear Employee,\n\n"
                    + "Please find attached your payslip.\n\n"
                    + "Password Format:\n"
                    + "First 2 letters of your name + last 2 digits of DOB year.\n\n"
                    + "Regards,\n"
                    + "Renwion HR Team");

            Employee employee = employeeRepository.findById(payroll.getEmployeeId())
                    .orElseThrow(() -> new IllegalStateException("Employee not found for id=" + payroll.getEmployeeId()));

            logger.info("Employee found: id={} name={}", employee.getEmpId(), employee.getName());

            YearMonth payrollMonth;

            try {
                payrollMonth = YearMonth.parse(
                        payroll.getMonth(),
                        new DateTimeFormatterBuilder()
                                .parseCaseInsensitive()
                                .appendPattern("MMMM yyyy")
                                .toFormatter(java.util.Locale.ENGLISH));
            } catch (DateTimeParseException e) {
                payrollMonth = YearMonth.parse(
                        payroll.getMonth(),
                        new DateTimeFormatterBuilder()
                                .parseCaseInsensitive()
                                .appendPattern("MMM yyyy")
                                .toFormatter(java.util.Locale.ENGLISH));
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

            ByteArrayInputStream pdfStream =
                    PayslipGenerator.generatePayslip(
                            payroll,
                            employee,
                            sickLeaveCount,
                            annualLeaveCount);

            byte[] pdfBytes = pdfStream.readAllBytes();
            InputStreamSource attachment = new ByteArrayResource(pdfBytes);
            helper.addAttachment("Payslip.pdf", attachment, "application/pdf");

            logger.info("Sending payslip email to {}", employeeEmail);
            mailSender.send(message);
            logger.info("Payslip email sent to {}", employeeEmail);

        } catch (Exception e) {
            logger.error("Failed to send payslip to {}: {}", employeeEmail, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}