package com.ozdilek.ecommerce.repository;

import com.ozdilek.ecommerce.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByUserId(String userId);
    
    void deleteByUserId(String userId);
    
    void deleteByExpiresAtBefore(LocalDateTime now);
}
