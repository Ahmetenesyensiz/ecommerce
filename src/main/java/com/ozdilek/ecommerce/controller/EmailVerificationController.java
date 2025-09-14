package com.ozdilek.ecommerce.controller;

import com.ozdilek.ecommerce.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmailVerificationController {
    
    private final EmailVerificationService emailVerificationService;
    
    @PostMapping("/verify/{token}")
    public ResponseEntity<Map<String, String>> verifyEmail(@PathVariable String token) {
        try {
            boolean verified = emailVerificationService.verifyEmail(token);
            
            if (verified) {
                return ResponseEntity.ok(Map.of(
                    "message", "Email verified successfully",
                    "status", "verified"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email verification failed",
                    "status", "failed"
                ));
            }
        } catch (Exception e) {
            log.error("Email verification error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "status", "error"
            ));
        }
    }
    
    @PostMapping("/resend")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required",
                "status", "unauthorized"
            ));
        }
        
        try {
            String email = authentication.getName();
            emailVerificationService.resendVerificationEmail(email);
            
            return ResponseEntity.ok(Map.of(
                "message", "Verification email sent successfully",
                "status", "sent"
            ));
        } catch (Exception e) {
            log.error("Resend verification email error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "status", "error"
            ));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getEmailVerificationStatus(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required",
                "status", "unauthorized"
            ));
        }
        
        try {
            String email = authentication.getName();
            boolean verified = emailVerificationService.isEmailVerified(email);
            
            return ResponseEntity.ok(Map.of(
                "email", email,
                "verified", verified,
                "status", "success"
            ));
        } catch (Exception e) {
            log.error("Get email verification status error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "status", "error"
            ));
        }
    }
}
