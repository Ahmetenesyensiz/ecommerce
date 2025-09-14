package com.ozdilek.ecommerce.controller;

import com.ozdilek.ecommerce.dto.auth.PhoneVerificationRequest;
import com.ozdilek.ecommerce.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/phone")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PhoneVerificationController {
    
    private final PhoneVerificationService phoneVerificationService;
    
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOTP(@RequestBody Map<String, String> request) {
        log.info("Send OTP request for phone: {}", request.get("phone"));
        
        String phone = request.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Phone number is required"));
        }
        
        try {
            String otp = phoneVerificationService.generateOTP(phone);
            
            // In production, don't return OTP in response
            return ResponseEntity.ok(Map.of(
                    "message", "OTP sent successfully",
                    "otp", otp // Remove this in production
            ));
        } catch (Exception e) {
            log.error("Error sending OTP: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send OTP"));
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOTP(@Valid @RequestBody PhoneVerificationRequest request) {
        log.info("Verify OTP request for phone: {}", request.getPhone());
        
        try {
            boolean verified = phoneVerificationService.verifyOTP(request.getPhone(), request.getOtp());
            
            if (verified) {
                return ResponseEntity.ok(Map.of(
                        "verified", true,
                        "message", "Phone number verified successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "verified", false,
                        "message", "Invalid or expired OTP"
                ));
            }
        } catch (Exception e) {
            log.error("Error verifying OTP: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to verify OTP"));
        }
    }
    
    @GetMapping("/status/{phone}")
    public ResponseEntity<Map<String, Object>> getVerificationStatus(@PathVariable String phone) {
        log.info("Get verification status for phone: {}", phone);
        
        try {
            boolean verified = phoneVerificationService.isPhoneVerified(phone);
            
            return ResponseEntity.ok(Map.of(
                    "phone", phone,
                    "verified", verified
            ));
        } catch (Exception e) {
            log.error("Error checking verification status: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to check verification status"));
        }
    }
}
