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
    public void sendWelcomeMail(
            String to,
            String employeeName,
            String temporaryPassword) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8");

            helper.setFrom("connect@renwion.in");
            helper.setTo(to);
            helper.setSubject("Welcome to Peopleflow Employee Portal");

            String body =
                    "<div style='font-family:Arial,sans-serif; color:#222; line-height:1.6;'>"

                    + "<div style='text-align:center; margin-bottom:20px;'>"
                    + "<img src='cid:renwionLogo' "
                    + "style='max-width:600px; width:100%; height:auto;' "
                    + "alt='Renwion'>"
                    + "</div>"

                    + "<p><strong>IMPORTANT: Do not share your employee mail or default password "
                    + "with anyone before completing your password reset. Treat this as confidential.</strong></p>"

                    + "<p>Dear " + employeeName + ",</p>"

                    + "<p>We are pleased to inform you that, going forward, "
                    + "<strong>some of our employee-related operations will be gradually transitioned "
                    + "to the Peopleflow portal</strong>.</p>"

                    + "<p>This initiative is being introduced to improve "
                    + "<strong>accuracy, transparency, centralized record-keeping, and efficiency</strong> "
                    + "in managing employee information and HR-related activities.</p>"

                    + "<p>You can access the portal using the link below:</p>"

                    + "<p><strong>Peopleflow Portal:</strong><br>"
                    + "<a href='https://peopleflow.renwion.in/login'>"
                    + "To access Peopleflow</a></p>"

                    + "<p>Your login credentials are:</p>"

                    + "<ul>"
                    + "<li><strong>Username:</strong> " + to + "</li>"
                    + "<li><strong>Temporary Password:</strong> " + temporaryPassword + "</li>"
                    + "<li>Please change the password as the above is a temporary password.</li>"
                    + "</ul>"

                    + "<h3>What you can access through Peopleflow</h3>"

                    + "<ul>"
                    + "<li>Employee profile and personal information</li>"
                    + "<li>Attendance and timesheets</li>"
                    + "<li><strong>Please fill the timesheet without reminders to avoid leave deductions.</strong></li>"
                    + "<li>Leave management</li>"
                    + "<li>Payslips and salary-related information</li>"
                    + "<li>Travel requests</li>"
                    + "<li>Other employee-related HR activities</li>"
                    + "</ul>"

                    + "<p>As part of the security process, <strong>Multi-Factor Authentication (MFA)</strong> "
                    + "is enabled. During your first login, you will be prompted to set up MFA using an "
                    + "authenticator application.</p>"

                    + "<p>As part of our ongoing efforts to improve the "
                    + "<strong>accuracy, security, and efficiency</strong> of our employee operations, "
                    + "we are gradually moving some of our day-to-day activities to the "
                    + "<strong>Peopleflow Employee Portal</strong>.</p>"

                    + "<h3>MFA Login Instructions</h3>"

                    + "<ul>"
                    + "<li><strong>For your first login, please use a laptop/desktop</strong> "
                    + "to access the PeopleFlow portal.</li>"
                    + "<li>During the first login, an <strong>MFA QR code will be displayed on the screen.</strong></li>"
                    + "<li>Please use your mobile phone and <strong>scan the QR code using Google Authenticator.</strong></li>"
                    + "<li>Once the QR code is scanned and MFA is set up, you can use the "
                    + "<strong>6-digit verification code</strong> generated in Google Authenticator for future logins.</li>"
                    + "<li><strong>Every time you log in to PeopleFlow, you will be required to enter "
                    + "the MFA verification code</strong> from your Google Authenticator app.</li>"
                    + "<li>After the initial setup, you can also <strong>log in to PeopleFlow from your mobile phone</strong> "
                    + "using the same MFA code.</li>"
                    + "<li>Please ensure that you keep your Google Authenticator app and MFA details secure "
                    + "and do not share your verification code with anyone.</li>"
                    + "</ul>"

                    + "<p>We request you to start using the portal for the applicable operations. "
                    + "This will help us maintain accurate and up-to-date records while gradually "
                    + "moving these processes to a centralized platform.</p>"

                    + "<p>If you experience any issues while accessing the portal, please reach out to "
                    + "<a href='mailto:connect@renwion.in'>connect@renwion.in</a>.</p>"

                    + "<p>Thank you for your cooperation and support.</p>"

                    + "<p>Best regards,<br>"
                    + "<strong>HR / People Operations</strong><br>"
                    + "<strong>Renwion</strong></p>"

                    + "</div>";

            helper.setText(body, true);

            org.springframework.core.io.ClassPathResource logo =
                    new org.springframework.core.io.ClassPathResource(
                            "static/images/renwion-email-header.png");

            helper.addInline(
                    "renwionLogo",
                    logo);

            mailSender.send(message);

            logger.info("Welcome mail sent to {}", to);

        } catch (Exception e) {

            logger.error(
                    "Failed to send welcome mail to {}: {}",
                    to,
                    e.getMessage(),
                    e);
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