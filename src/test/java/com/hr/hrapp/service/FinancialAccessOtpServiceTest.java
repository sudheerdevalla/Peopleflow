package com.hr.hrapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.hr.hrapp.entity.FinancialAccessOtp;
import com.hr.hrapp.repository.FinancialAccessOtpRepository;

@ExtendWith(MockitoExtension.class)
class FinancialAccessOtpServiceTest {

    @Mock
    private FinancialAccessOtpRepository otpRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private FinancialAccessOtpService financialAccessOtpService;

    @Test
    void shouldBlockOtpVerificationWhenExpired() {
        FinancialAccessOtp otp = new FinancialAccessOtp();
        otp.setUsername("user@example.com");
        otp.setOtpHash("encoded");
        otp.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        otp.setActive(true);

        when(otpRepository.findTopByUsernameAndActiveTrueOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(Optional.of(otp));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> financialAccessOtpService.verifyOtp("user@example.com", "123456"));

        assertEquals("OTP has expired. Please request a new one", ex.getMessage());
        verify(otpRepository).save(any(FinancialAccessOtp.class));
    }

    @Test
    void shouldIssueOtpAndSendMail() {
        when(otpRepository.findTopByUsernameAndActiveTrueOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        financialAccessOtpService.issueOtp("user@example.com", "user@example.com");

        verify(otpRepository).save(any(FinancialAccessOtp.class));
        verify(emailService).sendMail(anyString(), anyString(), anyString());
    }
}
