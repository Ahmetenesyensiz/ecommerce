package com.ozdilek.ecommerce.repository;

import com.ozdilek.ecommerce.model.PhoneVerification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneVerificationRepository extends MongoRepository<PhoneVerification, String> {
    
    Optional<PhoneVerification> findByPhone(String phone);
    
    void deleteByPhone(String phone);
    
    void deleteByExpiresAtBefore(java.time.LocalDateTime dateTime);
}
