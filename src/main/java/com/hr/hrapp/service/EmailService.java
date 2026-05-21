package com.hr.hrapp.service;

import com.hr.hrapp.entity.Salary;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSalaryMail(String toEmail, Salary salary) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Payslip - " + salary.getMonth());

        message.setText(
                "Salary Details:\n\n" +
                "Month: " + salary.getMonth() + "\n" +
                "Basic: " + salary.getBasicSalary() + "\n" +
                "Net: " + salary.getNetSalary() + "\n" +
                "Hike: " + salary.getHikeAmount()
        );

        mailSender.send(message);
    }
    public void sendMail(String to,
            String subject,
            String body) {

try {

MimeMessage message =
       mailSender.createMimeMessage();

MimeMessageHelper helper =
       new MimeMessageHelper(message, true);

helper.setTo(to);
helper.setSubject(subject);

helper.setText(body, true);

mailSender.send(message);

} catch (Exception e) {
e.printStackTrace();
}
}
}