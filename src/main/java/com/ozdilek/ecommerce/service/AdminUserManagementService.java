package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserManagementService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        log.info("Getting all users with pagination");
        return userRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        log.info("Getting user by id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
    
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String role) {
        log.info("Getting users by role: {}", role);
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(role))
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<User> getDeletedUsers() {
        log.info("Getting deleted users");
        return userRepository.findAll().stream()
                .filter(User::getDeleted)
                .toList();
    }
    
    @Transactional
    public User updateUserRole(String userId, List<String> newRoles) {
        log.info("Updating user roles for userId: {}, new roles: {}", userId, newRoles);
        
        User user = getUserById(userId);
        user.setRoles(newRoles);
        user = userRepository.save(user);
        
        log.info("User roles updated successfully for user: {}", user.getEmail());
        return user;
    }
    
    @Transactional
    public User banUser(String userId, String reason) {
        log.info("Banning user: {}, reason: {}", userId, reason);
        
        User user = getUserById(userId);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        
        // Add ban reason to metadata
        if (user.getMetadata() == null) {
            user.setMetadata(new java.util.HashMap<>());
        }
        ((java.util.Map<String, Object>) user.getMetadata()).put("banReason", reason);
        ((java.util.Map<String, Object>) user.getMetadata()).put("bannedAt", LocalDateTime.now());
        
        user = userRepository.save(user);
        log.info("User banned successfully: {}", user.getEmail());
        return user;
    }
    
    @Transactional
    public User unbanUser(String userId) {
        log.info("Unbanning user: {}", userId);
        
        User user = getUserById(userId);
        user.setDeleted(false);
        user.setDeletedAt(null);
        
        // Clear ban metadata
        if (user.getMetadata() != null) {
            ((java.util.Map<String, Object>) user.getMetadata()).remove("banReason");
            ((java.util.Map<String, Object>) user.getMetadata()).remove("bannedAt");
        }
        
        user = userRepository.save(user);
        log.info("User unbanned successfully: {}", user.getEmail());
        return user;
    }
    
    @Transactional
    public User hardDeleteUser(String userId) {
        log.info("Hard deleting user: {}", userId);
        
        User user = getUserById(userId);
        userRepository.delete(user);
        
        log.info("User hard deleted successfully: {}", user.getEmail());
        return user;
    }
    
    @Transactional(readOnly = true)
    public long getTotalUsersCount() {
        return userRepository.count();
    }
    
    @Transactional(readOnly = true)
    public long getActiveUsersCount() {
        return userRepository.findAll().stream()
                .filter(user -> !user.getDeleted())
                .count();
    }
    
    @Transactional(readOnly = true)
    public long getVerifiedUsersCount() {
        return userRepository.findAll().stream()
                .filter(user -> user.getIsVerified() && !user.getDeleted())
                .count();
    }
    
    @Transactional(readOnly = true)
    public List<User> getRecentUsers(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(since))
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        String lowerQuery = query.toLowerCase();
        return userRepository.findAll().stream()
                .filter(user -> 
                    user.getName().toLowerCase().contains(lowerQuery) ||
                    user.getEmail().toLowerCase().contains(lowerQuery) ||
                    (user.getPhone() != null && user.getPhone().contains(query))
                )
                .toList();
    }
}
