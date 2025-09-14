package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.PhoneVerification;
import com.ozdilek.ecommerce.repository.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneVerificationService {
    
    private final PhoneVerificationRepository phoneVerificationRepository;
    
    public String generateOTP(String phone) {
        log.info("Generating OTP for phone: {}", phone);
        
        // Delete existing verification for this phone
        phoneVerificationRepository.deleteByPhone(phone);
        
        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        // Save verification record
        PhoneVerification verification = PhoneVerification.builder()
                .phone(phone)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5)) // 5 minutes expiry
                .attempts(0)
                .verified(false)
                .build();
        
        phoneVerificationRepository.save(verification);
        
        // In real implementation, send SMS here
        log.info("OTP generated for phone {}: {}", phone, otp);
        
        return otp; // For testing purposes
    }
    
    public boolean verifyOTP(String phone, String otp) {
        log.info("Verifying OTP for phone: {}", phone);
        
        Optional<PhoneVerification> verificationOpt = phoneVerificationRepository.findByPhone(phone);
        
        if (verificationOpt.isEmpty()) {
            log.warn("No verification record found for phone: {}", phone);
            return false;
        }
        
        PhoneVerification verification = verificationOpt.get();
        
        // Check if expired
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for phone: {}", phone);
            phoneVerificationRepository.delete(verification);
            return false;
        }
        
        // Check attempts
        if (verification.getAttempts() >= verification.getMaxAttempts()) {
            log.warn("Max attempts exceeded for phone: {}", phone);
            phoneVerificationRepository.delete(verification);
            return false;
        }
        
        // Increment attempts
        verification.setAttempts(verification.getAttempts() + 1);
        
        if (verification.getOtp().equals(otp)) {
            verification.setVerified(true);
            phoneVerificationRepository.save(verification);
            log.info("OTP verified successfully for phone: {}", phone);
            return true;
        } else {
            phoneVerificationRepository.save(verification);
            log.warn("Invalid OTP for phone: {}", phone);
            return false;
        }
    }
    
    public boolean isPhoneVerified(String phone) {
        Optional<PhoneVerification> verificationOpt = phoneVerificationRepository.findByPhone(phone);
        
        if (verificationOpt.isEmpty()) {
            return false;
        }
        
        PhoneVerification verification = verificationOpt.get();
        return verification.getVerified() && verification.getExpiresAt().isAfter(LocalDateTime.now());
    }
    
    public void cleanupExpiredVerifications() {
        phoneVerificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired phone verifications");
    }
}
