package com.gupshup.gupshup_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.gupshup.gupshup_backend.controller.ContactRequest; 

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async // Ye method background me chalega
    public void sendContactEmailBackground(ContactRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("mrabhiverma000@gmail.com"); 
            message.setSubject("New Web Inquiry from: " + request.getName());
            
            String mailContent = "You have a new message from your landing page:\n\n"
                               + "Name: " + request.getName() + "\n"
                               + "Email: " + request.getEmail() + "\n"
                               + "Message: \n" + request.getMessage();
            
            message.setText(mailContent);
            mailSender.send(message);
            System.out.println("Background Email sent successfully!");
        } catch (Exception e) {
            System.err.println("Error sending background email: " + e.getMessage());
        }
    }
}