package com.gupshup.gupshup_backend.service;

import com.gupshup.gupshup_backend.model.User;
import com.gupshup.gupshup_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // 👇 application.properties se key uthayega
    @Value("${fast2sms.api.key}")
    private String apiKey;

    private Map<String, String> otpStorage = new HashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    // 1️⃣ SEND OTP (Ab Real SMS jayega 🚀)
    public String generateAndSendOtp(String phoneNumber) {
        // Step 1: Check user exist or not
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // Step 2: Generate OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));
        otpStorage.put(phoneNumber, otp);

        // Step 3: Send Real SMS via Fast2SMS
        sendSms(phoneNumber, otp);

        System.out.println("🔔 OTP Sent to " + phoneNumber + ": " + otp);
        return otp;
    }

    // 📩 REAL SMS FUNCTION
    private void sendSms(String phoneNumber, String otp) {
        try {

            String cleanNumber = phoneNumber.replace("+91", "").replaceAll("\\s+", "");
            // Fast2SMS URL (OTP Route)
            // variables_values: Ye OTP hai jo message me jayega
            // numbers: Mobile number
            // String url = "https://www.fast2sms.com/dev/bulkV2?authorization=" + apiKey + 
            //              "&route=otp&variables_values=" + otp + 
            //              "&flash=0&numbers=" + cleanNumber;

            String url = "https://www.fast2sms.com/dev/bulkV2?authorization=" + apiKey + 
                         "&route=v3&variables_values=" + otp + 
                         "&numbers=" + cleanNumber;

            // API Call
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("📨 SMS API Response: " + response);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send SMS: " + e.getMessage());
            // Development ke liye print kar rahe hain taki kaam na ruke
            System.out.println("⚠️ Use this OTP locally: " + otp);
        }
    }

    // 2️⃣ REGISTER USER (Same as before)
    public User registerUser(User user) {
        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("Phone number already registered!");
        }
        return userRepository.save(user);
    }

    // 3️⃣ VERIFY OTP (Same as before)
    public boolean verifyOtp(String phoneNumber, String otp) {
        if (otpStorage.containsKey(phoneNumber) && otpStorage.get(phoneNumber).equals(otp)) {
            otpStorage.remove(phoneNumber); 
            
            // STRICT CHECK
            Optional<User> existingUser = userRepository.findByPhoneNumber(phoneNumber);
            return existingUser.isPresent();
        }
        return false;
    }
}