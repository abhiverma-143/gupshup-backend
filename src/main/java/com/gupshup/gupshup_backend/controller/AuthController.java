package com.gupshup.gupshup_backend.controller;

import com.gupshup.gupshup_backend.model.User;
import com.gupshup.gupshup_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Signup API (Same as before)
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            authService.registerUser(user);
            return ResponseEntity.ok(Map.of("message", "User registered successfully!", "status", "success"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "status", "error"));
        }
    }

    // 👉 SEND OTP API (UPDATED)
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        
        try {
            String otp = authService.generateAndSendOtp(phoneNumber);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully", "otp", otp));
        } catch (RuntimeException e) {
            // 👇 Agar user nahi mila, to 404 Not Found bhejo
            return ResponseEntity.status(404).body(Map.of("message", "Number not registered. Please SignUp first.", "status", "error"));
        }
    }

    // Verify OTP API (Same as before)
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String otp = request.get("otp");

        boolean isValid = authService.verifyOtp(phoneNumber, otp);

        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "Login Successful", "status", "success"));
        } else {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid OTP", "status", "error"));
        }
    }
}