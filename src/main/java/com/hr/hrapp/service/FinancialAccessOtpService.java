package com.hr.hrapp.service;

import com.hr.hrapp.entity.FinancialAccessOtp;
import com.hr.hrapp.repository.FinancialAccessOtpRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class FinancialAccessOtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int OTP_RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int MAX_RESEND_COUNT = 3;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private FinancialAccessOtpRepository otpRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void issueOtp(String username, String email) {
        FinancialAccessOtp existing = otpRepository.findTopByUsernameAndActiveTrueOrderByCreatedAtDesc(username)
                .orElse(null);

        if (existing != null) {

            LocalDateTime now = LocalDateTime.now();

            // Expired challenge should never block a new OTP request
            if (existing.getExpiresAt() != null
                    && !existing.getExpiresAt().isAfter(now)) {

                existing.setActive(false);
                otpRepository.save(existing);

                existing = null;
            }
        }

        if (existing != null) {

            if (existing.getResendAllowedAt() != null
                    && existing.getResendAllowedAt().isAfter(LocalDateTime.now())) {

                long secondsLeft = Duration.between(
                        LocalDateTime.now(),
                        existing.getResendAllowedAt()
                ).getSeconds();

                throw new IllegalStateException(
                        "Please wait "
                                + Math.max(1, secondsLeft)
                                + " seconds before requesting another OTP"
                );
            }

            if (existing.getResendCount() >= MAX_RESEND_COUNT) {

                throw new IllegalStateException(
                        "OTP resend limit reached. Please wait for the current OTP to expire and try again"
                );
            }

            existing.setActive(false);
            otpRepository.save(existing);
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        FinancialAccessOtp challenge = new FinancialAccessOtp();
        challenge.setUsername(username);
        challenge.setEmail(email);
        challenge.setOtpHash(passwordEncoder.encode(otp));
        challenge.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        challenge.setResendAllowedAt(LocalDateTime.now().plusSeconds(OTP_RESEND_COOLDOWN_SECONDS));
        challenge.setFailedAttempts(0);
        challenge.setResendCount(existing == null ? 1 : existing.getResendCount() + 1);
        challenge.setVerified(false);
        challenge.setActive(true);
        otpRepository.save(challenge);

        emailService.sendMail(
                email,
                "PeopleFlow Financial Access OTP",
                "<p>Your OTP for PeopleFlow financial access is <strong>" + otp + "</strong>.</p>"
                        + "<p>This OTP expires in " + OTP_EXPIRY_MINUTES + " minutes.</p>"
                        + "<p>If you did not request this, please contact HR immediately.</p>"
        );
    }

    @Transactional
    public void verifyOtp(String username, String submittedOtp) {
        FinancialAccessOtp challenge = otpRepository.findTopByUsernameAndActiveTrueOrderByCreatedAtDesc(username)
                .orElseThrow(() -> new IllegalStateException("No active OTP challenge found"));

        if (challenge.getExpiresAt() == null || challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            challenge.setActive(false);
            otpRepository.save(challenge);
            throw new IllegalStateException("OTP has expired. Please request a new one");
        }

        if (challenge.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            challenge.setActive(false);
            otpRepository.save(challenge);
            throw new IllegalStateException("OTP retry limit reached. Please request a new OTP");
        }

        if (!passwordEncoder.matches(submittedOtp, challenge.getOtpHash())) {
            challenge.setFailedAttempts(challenge.getFailedAttempts() + 1);
            if (challenge.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                challenge.setActive(false);
            }
            otpRepository.save(challenge);
            throw new IllegalArgumentException("Invalid OTP");
        }

        challenge.setVerified(true);
        challenge.setVerifiedAt(LocalDateTime.now());
        challenge.setActive(false);
        otpRepository.save(challenge);
    }

    public FinancialAccessOtp getCurrentChallenge(String username) {
        return otpRepository.findTopByUsernameAndActiveTrueOrderByCreatedAtDesc(username).orElse(null);
    }

    public String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "your registered email";
        }
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String masked = name.length() <= 2 ? name.charAt(0) + "*" : name.substring(0, 2) + "***";
        return masked + "@" + parts[1];
    }
}
