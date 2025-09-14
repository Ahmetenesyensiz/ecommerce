package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletionService {
    
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    
    @Transactional
    public void softDeleteUser(String email) {
        log.info("Soft deleting user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete profile image if exists
        if (user.getProfileImageUrl() != null) {
            try {
                fileUploadService.deleteProfileImage(user.getProfileImageUrl(), user.getId());
            } catch (Exception e) {
                log.warn("Failed to delete profile image during user deletion: {}", e.getMessage());
            }
        }
        
        // Soft delete user
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        
        // Anonymize sensitive data
        user.setEmail("deleted_" + user.getId() + "@deleted.com");
        user.setName("Deleted User");
        user.setPhone(null);
        user.setProfileImageUrl(null);
        user.setAddresses(null);
        
        userRepository.save(user);
        
        log.info("User soft deleted successfully: {}", email);
    }
    
    @Transactional
    public void restoreUser(String userId) {
        log.info("Restoring user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.getDeleted()) {
            throw new RuntimeException("User is not deleted");
        }
        
        user.setDeleted(false);
        user.setDeletedAt(null);
        
        userRepository.save(user);
        
        log.info("User restored successfully: {}", userId);
    }
    
    @Transactional
    public void permanentDeleteUser(String userId) {
        log.info("Permanently deleting user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete profile image if exists
        if (user.getProfileImageUrl() != null) {
            try {
                fileUploadService.deleteProfileImage(user.getProfileImageUrl(), user.getId());
            } catch (Exception e) {
                log.warn("Failed to delete profile image during permanent deletion: {}", e.getMessage());
            }
        }
        
        userRepository.delete(user);
        
        log.info("User permanently deleted: {}", userId);
    }
    
    public boolean isUserDeleted(String email) {
        User user = userRepository.findByEmail("deleted_" + email + "@deleted.com").orElse(null);
        return user != null && user.getDeleted();
    }
}
