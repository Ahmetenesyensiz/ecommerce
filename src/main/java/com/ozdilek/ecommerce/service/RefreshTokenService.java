package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.RefreshToken;
import com.ozdilek.ecommerce.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;
    
    public RefreshToken saveRefreshToken(String userId, String token) {
        log.info("Saving refresh token for user: {}", userId);
        
        // Delete existing refresh tokens for user
        refreshTokenRepository.deleteByUserId(userId);
        
        String tokenHash = hashToken(token);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshExpiration / 1000);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .issuedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .revoked(false)
                .build();
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    public Optional<RefreshToken> findByToken(String token) {
        String tokenHash = hashToken(token);
        return refreshTokenRepository.findByTokenHash(tokenHash);
    }
    
    public void revokeToken(String token) {
        log.info("Revoking refresh token");
        Optional<RefreshToken> refreshToken = findByToken(token);
        if (refreshToken.isPresent()) {
            RefreshToken tokenEntity = refreshToken.get();
            tokenEntity.setRevoked(true);
            refreshTokenRepository.save(tokenEntity);
        }
    }
    
    public void revokeAllUserTokens(String userId) {
        log.info("Revoking all refresh tokens for user: {}", userId);
        refreshTokenRepository.deleteByUserId(userId);
    }
    
    public boolean isTokenValid(String token) {
        Optional<RefreshToken> refreshToken = findByToken(token);
        return refreshToken.isPresent() && 
               !refreshToken.get().getRevoked() && 
               refreshToken.get().getExpiresAt().isAfter(LocalDateTime.now());
    }
    
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
