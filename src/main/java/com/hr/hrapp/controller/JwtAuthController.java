package com.hr.hrapp.controller;

import com.hr.hrapp.entity.User;
import com.hr.hrapp.repository.UserRepository;
import com.hr.hrapp.security.JwtUtil;
import com.hr.hrapp.service.CustomerUserDetailsService;
import com.hr.hrapp.service.MfaService;
// ...existing code...
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

// ...existing code...
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
    private MfaService mfaService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            password
                    )
            );
            User user = userRepository
                    .findByUsername(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found"));

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(user.getUsername());

            String token =
                    jwtUtil.generateToken(
                            user.getUsername(),
                            userDetails.getAuthorities());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }


    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String codeStr = data.get("code");

        if (username == null || codeStr == null) {
            return ResponseEntity.badRequest().body("Username and MFA code are required");
        }

        try {
            int code = Integer.parseInt(codeStr);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!user.isMfaEnabled() || user.getTotpSecret() == null) {
                return ResponseEntity.badRequest().body("MFA is not enabled for this user");
            }

            if (!mfaService.verifyCode(user.getTotpSecret(), code)) {
                return ResponseEntity.status(401).body("Invalid MFA code");
            }

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(user.getUsername());

            String token = jwtUtil.generateToken(
                    user.getUsername(),
                    userDetails.getAuthorities());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("MFA code must be a 6-digit number");
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