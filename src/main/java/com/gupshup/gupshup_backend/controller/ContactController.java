package com.gupshup.gupshup_backend.controller;

import com.gupshup.gupshup_backend.service.EmailService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = {"http://localhost:3000", "https://gupshup-frontend.vercel.app"})
public class ContactController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody ContactRequest request) {
        
        // Service ko background me email bhejne ka order de do
        emailService.sendContactEmailBackground(request);

        // Turant React ko "Success" return kar do!
        return ResponseEntity.ok("Message queued successfully!");
    }
}