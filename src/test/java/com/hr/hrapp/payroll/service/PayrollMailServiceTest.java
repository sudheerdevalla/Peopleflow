package com.hr.hrapp.payroll.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.repository.EmployeeRepository;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
public class PayrollMailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private PayrollMailService payrollMailService;

    @Test
    public void sendPayslip_attachesPdfAndSends() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Employee e = new Employee();
        e.setEmpId(123L);
        e.setName("John Doe");
        e.setDateOfBirth(LocalDate.of(1990, 1, 1));

        when(employeeRepository.findById(123L)).thenReturn(Optional.of(e));

        Payroll p = new Payroll();
        p.setEmployeeId(123L);
        p.setEmployeeName("John Doe");
        p.setMonth("June 2026");
        p.setBasicSalary(1000);

        // ensure mailFrom is set (injected via @Value in production)
        ReflectionTestUtils.setField(payrollMailService, "mailFrom", "test@domain.local");

        payrollMailService.sendPayslip(p, "john@example.com");

        verify(mailSender).send(mimeMessage);

        Object content = mimeMessage.getContent();
        assertTrue(content instanceof Multipart, "MimeMessage content should be a Multipart");
        Multipart mp = (Multipart) content;
        boolean hasAttachment = false;
        for (int i = 0; i < mp.getCount(); i++) {
            BodyPart part = mp.getBodyPart(i);
            String disposition = part.getDisposition();
            if (disposition != null && disposition.equalsIgnoreCase(BodyPart.ATTACHMENT)) {
                hasAttachment = true;
                break;
            }
            if (part.getFileName() != null && !part.getFileName().isBlank()) {
                hasAttachment = true;
                break;
            }
        }

        assertTrue(hasAttachment, "Expected an attachment part in the sent MimeMessage");
    }
}
