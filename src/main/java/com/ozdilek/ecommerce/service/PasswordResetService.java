package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.dto.auth.ForgotPasswordRequest;
import com.ozdilek.ecommerce.dto.auth.ResetPasswordRequest;
import com.ozdilek.ecommerce.model.PasswordResetToken;
import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.PasswordResetTokenRepository;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final int TOKEN_EXPIRATION_MINUTES = 15; // 15 dakika
    
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));
        
        // Delete existing tokens for this user
        tokenRepository.deleteByUserId(user.getId());
        
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES);
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .used(false)
                .build();
        
        tokenRepository.save(resetToken);
        
        // TODO: Send email with reset link
        log.info("Password reset token generated for user: {}. Token expires in {} minutes", 
                user.getEmail(), TOKEN_EXPIRATION_MINUTES);
    }
    
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        
        if (resetToken.getUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }
        
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }
        
        // Mark token as used
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);
        
        // Update user password
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password reset successfully for user: {}", user.getEmail());
    }
    
    public boolean isTokenValid(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);
        
        if (resetToken == null || resetToken.getUsed()) {
            return false;
        }
        
        return !resetToken.getExpiresAt().isBefore(LocalDateTime.now());
    }
    
    public long getTokenExpirationTime(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);
        
        if (resetToken == null) {
            return 0;
        }
        
        return java.time.Duration.between(LocalDateTime.now(), resetToken.getExpiresAt()).toMinutes();
    }
    
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired password reset tokens");
    }
}
