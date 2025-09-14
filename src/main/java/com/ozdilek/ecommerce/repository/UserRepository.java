package com.ozdilek.ecommerce.repository;

import com.ozdilek.ecommerce.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    @Query("{'email': ?0, 'deleted': {$ne: true}}")
    Optional<User> findByEmail(String email);
    
    @Query("{'email': ?0, 'deleted': {$ne: true}}")
    boolean existsByEmail(String email);
    
    @Query("{'email': ?0, 'isVerified': true, 'deleted': {$ne: true}}")
    Optional<User> findByEmailAndIsVerifiedTrue(String email);
    
    @Query("{'deleted': {$ne: true}}")
    List<User> findAllActiveUsers();
    
    @Query("{'deleted': true}")
    List<User> findAllDeletedUsers();
    
    @Query("{'_id': ?0, 'deleted': {$ne: true}}")
    Optional<User> findActiveUserById(String id);
}
