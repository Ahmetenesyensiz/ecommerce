package com.ozdilek.ecommerce.repository;

import com.ozdilek.ecommerce.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    
    List<RefreshToken> findByUserId(String userId);
    
    void deleteByUserId(String userId);
    
    void deleteByTokenHash(String tokenHash);
}
