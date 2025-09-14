package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.EmailVerificationToken;
import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.EmailVerificationTokenRepository;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    
    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    
    private static final int TOKEN_EXPIRATION_HOURS = 24;
    
    @Transactional
    public EmailVerificationToken generateVerificationToken(String userId, String email) {
        // Delete existing tokens for this user
        tokenRepository.deleteByUserId(userId);
        
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);
        
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .userId(userId)
                .email(email)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .used(false)
                .build();
        
        tokenRepository.save(verificationToken);
        log.info("Generated email verification token for user: {}", email);
        
        return verificationToken;
    }
    
    @Transactional
    public boolean verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        if (verificationToken.getUsed()) {
            throw new RuntimeException("Token has already been used");
        }
        
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }
        
        // Mark token as used
        verificationToken.setUsed(true);
        verificationToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);
        
        // Update user as verified
        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsVerified(true);
        userRepository.save(user);
        
        log.info("Email verified successfully for user: {}", user.getEmail());
        return true;
    }
    
    public boolean isEmailVerified(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return user.getIsVerified();
    }
    
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getIsVerified()) {
            throw new RuntimeException("Email is already verified");
        }
        
        generateVerificationToken(user.getId(), email);
        // TODO: Send email notification
        log.info("Resent verification email for user: {}", email);
    }
    
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired email verification tokens");
    }
}
