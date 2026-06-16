package com.hr.hrapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void testSendMail() {

        MimeMessage mimeMessage =
                new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        assertDoesNotThrow(() ->
                emailService.sendMail(
                        "test@example.com",
                        "Test Subject",
                        "Test Body"));

    }
}