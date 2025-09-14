package com.ozdilek.ecommerce.repository;

import com.ozdilek.ecommerce.model.EmailVerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends MongoRepository<EmailVerificationToken, String> {
    
    Optional<EmailVerificationToken> findByToken(String token);
    
    Optional<EmailVerificationToken> findByUserId(String userId);
    
    void deleteByUserId(String userId);
    
    void deleteByExpiresAtBefore(LocalDateTime now);
}
