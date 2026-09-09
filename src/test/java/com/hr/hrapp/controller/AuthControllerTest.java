package com.hr.hrapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.hr.hrapp.entity.User;
import com.hr.hrapp.repository.CandidateRepository;
import com.hr.hrapp.repository.CompanyUpdateRepository;
import com.hr.hrapp.repository.EmployeeAttendanceRepository;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.LeaveRepository;
import com.hr.hrapp.repository.TravelRequestRepository;
import com.hr.hrapp.repository.UserRepository;
import com.hr.hrapp.service.EmailService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeAttendanceRepository employeeAttendanceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder encoder;
    @Mock
    private LeaveRepository leaveRepository;
    @Mock
    private TravelRequestRepository travelRequestRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private CompanyUpdateRepository companyUpdateRepository;
    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerUser_allowsFreshUniqueEmail() {
        when(userRepository.findByUsername("fresh@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("Pass@123")).thenReturn("encoded-password");

        String result = authController.registerUser("Fresh User", "fresh@example.com", "Pass@123");

        assertEquals("redirect:/login", result);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertFalse(userCaptor.getValue().isForcePasswordChange());
        assertFalse(userCaptor.getValue().isMfaEnabled());
        verify(employeeRepository).save(any());
    }

    @Test
    void registerUser_blocksExistingEmail() {
        User existing = new User();
        existing.setUsername("existing@example.com");
        when(userRepository.findByUsername("existing@example.com")).thenReturn(Optional.of(existing));

        String result = authController.registerUser("Existing User", "existing@example.com", "Pass@123");

        assertEquals("redirect:/register?error", result);
    }
}
