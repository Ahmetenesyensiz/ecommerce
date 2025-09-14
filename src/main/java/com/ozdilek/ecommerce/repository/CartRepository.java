package com.ozdilek.ecommerce.repository;

import com.ozdilek.ecommerce.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    
    Optional<Cart> findByUserId(String userId);
    
    Optional<Cart> findBySessionId(String sessionId);
    
    void deleteBySessionId(String sessionId);
}
