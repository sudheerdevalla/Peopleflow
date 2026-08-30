package com.hr.hrapp.controller;

import com.hr.hrapp.entity.User;
import com.hr.hrapp.repository.UserRepository;
import com.hr.hrapp.service.MfaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

@Controller
public class MfaController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MfaService mfaService;

    @PostMapping("/api/mfa/setup")
    @ResponseBody
    public ResponseEntity<?> setupMfa(@RequestParam String username) {

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String secret = user.getTotpSecret();
            if (secret == null || secret.isBlank()) {
                secret = mfaService.generateSecret();
            }

            user.setTotpSecret(secret);
            user.setMfaEnabled(true);
            userRepository.save(user);

            String accountName = URLEncoder.encode(
                    username, StandardCharsets.UTF_8);

            String issuer = URLEncoder.encode(
                    "PeopleFlow", StandardCharsets.UTF_8);

            String otpAuthUrl =
                    "otpauth://totp/" + issuer + ":" + accountName
                    + "?secret=" + secret
                    + "&issuer=" + issuer;

            BitMatrix matrix = new MultiFormatWriter().encode(
                    otpAuthUrl,
                    BarcodeFormat.QR_CODE,
                    300,
                    300);

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    outputStream);

            String qrCode = Base64.getEncoder()
                    .encodeToString(outputStream.toByteArray());

            Map<String, String> response = new HashMap<>();
            response.put("message", "MFA secret generated successfully");
            response.put("secret", secret);
            response.put("qrCode", qrCode);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("MFA setup failed: " + e.getMessage());
        }
    }
}
