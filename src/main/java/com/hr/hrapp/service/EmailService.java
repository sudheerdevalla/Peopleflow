package com.hr.hrapp.service;

import com.hr.hrapp.entity.Salary;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.core.io.ByteArrayResource;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // In-memory recent send guard to prevent accidental double sends within short window
    private final ConcurrentHashMap<String, Long> recentSends = new ConcurrentHashMap<>();

    public void sendSalaryMail(String toEmail, Salary salary) {

        if (salary == null) {
            logger.warn("Attempted to send salary mail but salary is null for recipient={}", toEmail);
            return;
        }

        String key = "salary:" + (salary.getId() != null ? salary.getId() : salary.getMonth()) + ":to:" + toEmail;
        long now = System.currentTimeMillis();
        Long last = recentSends.get(key);
        if (last != null && now - last < TimeUnit.SECONDS.toMillis(30)) {
            logger.info("Skipping duplicate salary email to {} for key={} (within guard window)", toEmail, key);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("connect@renwion.in");
            helper.setTo(toEmail);
            helper.setSubject("Payslip - " + salary.getMonth());

            StringBuilder sb = new StringBuilder();
            sb.append("<h2>Payslip - ").append(salary.getMonth()).append("</h2>");
            sb.append("<p><strong>Basic:</strong> ₹ ").append(String.format("%.2f", salary.getBasicSalary())).append("</p>");
            sb.append("<p><strong>Net:</strong> ₹ ").append(String.format("%.2f", salary.getNetSalary())).append("</p>");
            sb.append("<p><strong>Hike:</strong> ₹ ").append(String.format("%.2f", salary.getHikeAmount())).append("</p>");

            helper.setText(sb.toString(), true);

            mailSender.send(message);
            recentSends.put(key, now);
            logger.info("Sent salary mail to {} for salaryId={}", toEmail, salary.getId());

        } catch (Exception e) {
            logger.error("Failed to send salary mail to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendMail(String to,
            String subject,
            String body) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("connect@renwion.in");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            logger.info("Sent mail to {} subject={}", to, subject);

        } catch (Exception e) {
            logger.error("Failed to send mail to {} subject={}: {}", to, subject, e.getMessage(), e);
        }
    }
    public void sendMailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] fileData,
            String fileName) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8");

            helper.setFrom("connect@renwion.in");
            helper.setTo(to);

            helper.setSubject(subject);

            helper.setText(body, true);

            helper.addAttachment(
                    fileName,
                    new ByteArrayResource(fileData));

            mailSender.send(message);

            logger.info(
                    "Mail with attachment sent to {}",
                    to);

        } catch (Exception e) {

            logger.error(
                    "Failed attachment mail : {}",
                    e.getMessage(),
                    e);
        }
    }
}