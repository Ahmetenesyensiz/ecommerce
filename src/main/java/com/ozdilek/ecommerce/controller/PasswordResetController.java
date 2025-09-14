package com.ozdilek.ecommerce.controller;

import com.ozdilek.ecommerce.dto.auth.ForgotPasswordRequest;
import com.ozdilek.ecommerce.dto.auth.ResetPasswordRequest;
import com.ozdilek.ecommerce.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.requestPasswordReset(request);
            
            return ResponseEntity.ok(Map.of(
                "message", "Password reset instructions sent to your email",
                "status", "success"
            ));
        } catch (Exception e) {
            log.error("Forgot password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "status", "error"
            ));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request);
            
            return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully",
                "status", "success"
            ));
        } catch (Exception e) {
            log.error("Reset password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "status", "error"
            ));
        }
    }
    
    @GetMapping("/reset-password/validate/{token}")
    public ResponseEntity<Map<String, Object>> validateResetToken(@PathVariable String token) {
        try {
            boolean isValid = passwordResetService.isTokenValid(token);
            long expirationTime = passwordResetService.getTokenExpirationTime(token);
            
            return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "expiresInMinutes", expirationTime,
                "status", "success"
            ));
        } catch (Exception e) {
            log.error("Validate reset token error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "status", "error"
            ));
        }
    }
}
