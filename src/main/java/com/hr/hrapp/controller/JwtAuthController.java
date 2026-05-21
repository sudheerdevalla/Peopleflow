package com.hr.hrapp.controller;

import com.hr.hrapp.entity.User;
import com.hr.hrapp.repository.UserRepository;
import com.hr.hrapp.security.JwtUtil;
import com.hr.hrapp.service.CustomerUserDetailsService;
import com.hr.hrapp.service.AuditLogService;
import com.hr.hrapp.entity.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class JwtAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private CustomerUserDetailsService userDetailsService;

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request) {
        
        String endpoint = request.getRequestURI();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            password
                    )
            );
            User user = userRepository.findByUsername(username);
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtUtil.generateToken(user.getUsername(), userDetails.getAuthorities());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            // audit success
            AuditLog log = new AuditLog(username, "LOGIN", endpoint, LocalDateTime.now(), "SUCCESS");
            auditLogService.save(log);
            return ResponseEntity.ok("Login successful");
        } catch (AuthenticationException e) {
            // audit failure
            AuditLog log = new AuditLog(username, "LOGIN", endpoint, LocalDateTime.now(), "FAILURE");
            auditLogService.save(log);
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerData) {
        if (userRepository.findByUsername(registerData.get("username")) != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        User user = new User();
        user.setUsername(registerData.get("username"));
        user.setPassword(encoder.encode(registerData.get("password")));
        user.setRole("USER");
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }
}