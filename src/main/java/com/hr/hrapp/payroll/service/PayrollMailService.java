package com.hr.hrapp.payroll.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.util.PayslipGenerator;
import com.hr.hrapp.repository.EmployeeRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class PayrollMailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmployeeRepository employeeRepository;

    public void sendPayslip(
            Payroll payroll,
            String employeeEmail) {

        try {

            // =========================
            // DEBUG START
            // =========================

            System.out.println(
                    "MAIL METHOD STARTED");

            System.out.println(
                    "Employee Email: "
                    + employeeEmail);

            // =========================
            // CREATE MESSAGE
            // =========================

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true);

            helper.setTo(employeeEmail);

            helper.setSubject(
                    "PeopleFlow Payslip - "
                    + payroll.getMonth());

            helper.setText(
                    "Dear Employee,\n\n"
                    + "Please find attached your payslip.\n\n"
                    + "Password Format:\n"
                    + "First 2 letters of your name + "
                    + "last 2 digits of DOB year.\n\n"
                    + "Regards,\n"
                    + "PeopleFlow HR Team");

            // =========================
            // FETCH EMPLOYEE
            // =========================

            Employee employee =
                    employeeRepository
                    .findById(
                            payroll.getEmployeeId())
                    .orElseThrow();

            System.out.println(
                    "Employee Found: "
                    + employee.getName());

            // =========================
            // PDF GENERATE
            // =========================

            ByteArrayInputStream pdfStream =
                    PayslipGenerator
                    .generatePayslip(
                            payroll,
                            employee);

            // =========================
            // TEMP FILE CREATE
            // =========================

            File tempFile =
                    File.createTempFile(
                            "payslip",
                            ".pdf");

            FileOutputStream fos =
                    new FileOutputStream(
                            tempFile);

            fos.write(
                    pdfStream.readAllBytes());

            fos.close();

            // =========================
            // ATTACH PDF
            // =========================

            helper.addAttachment(
                    "Payslip.pdf",
                    tempFile);

            // =========================
            // SEND MAIL
            // =========================

            System.out.println(
                    "TRYING TO SEND MAIL");

            mailSender.send(message);

            System.out.println(
                    "MAIL SENT SUCCESSFULLY");

        } catch (Exception e) {

            System.out.println(
                    "MAIL FAILED");

            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }
}