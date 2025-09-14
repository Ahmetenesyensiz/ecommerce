package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.dto.user.ProfileUpdateRequest;
import com.ozdilek.ecommerce.dto.user.UserProfileResponse;
import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;
    
    public UserProfileResponse getUserProfile(String email) {
        log.info("Getting user profile for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return mapToUserProfileResponse(user);
    }
    
    @Transactional
    public UserProfileResponse updateUserProfile(String email, ProfileUpdateRequest request) {
        log.info("Updating user profile for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update basic info
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        // Update password if provided
        if (request.getCurrentPassword() != null && request.getNewPassword() != null) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }
        
        user = userRepository.save(user);
        log.info("User profile updated successfully for email: {}", email);
        
        return mapToUserProfileResponse(user);
    }
    
    @Transactional
    public void addUserAddress(String email, User.Address address) {
        log.info("Adding address for user email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getAddresses() == null) {
            user.setAddresses(new java.util.ArrayList<>());
        }
        
        // Set as default if it's the first address
        if (user.getAddresses().isEmpty()) {
            address.setIsDefault(true);
        } else if (address.getIsDefault() != null && address.getIsDefault()) {
            // Remove default from other addresses
            user.getAddresses().forEach(addr -> addr.setIsDefault(false));
        }
        
        user.getAddresses().add(address);
        userRepository.save(user);
        
        log.info("Address added successfully for user email: {}", email);
    }
    
    @Transactional
    public void updateUserAddress(String email, String addressId, User.Address updatedAddress) {
        log.info("Updating address {} for user email: {}", addressId, email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getAddresses() == null) {
            throw new RuntimeException("User has no addresses");
        }
        
        User.Address existingAddress = user.getAddresses().stream()
                .filter(addr -> addr.getLabel().equals(addressId)) // Using label as ID for simplicity
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        // Update address fields
        existingAddress.setLabel(updatedAddress.getLabel());
        existingAddress.setLine1(updatedAddress.getLine1());
        existingAddress.setLine2(updatedAddress.getLine2());
        existingAddress.setCity(updatedAddress.getCity());
        existingAddress.setPostalCode(updatedAddress.getPostalCode());
        existingAddress.setCountry(updatedAddress.getCountry());
        
        // Handle default address change
        if (updatedAddress.getIsDefault() != null && updatedAddress.getIsDefault()) {
            user.getAddresses().forEach(addr -> addr.setIsDefault(false));
            existingAddress.setIsDefault(true);
        }
        
        userRepository.save(user);
        log.info("Address updated successfully for user email: {}", email);
    }
    
    @Transactional
    public void deleteUserAddress(String email, String addressId) {
        log.info("Deleting address {} for user email: {}", addressId, email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getAddresses() == null) {
            throw new RuntimeException("User has no addresses");
        }
        
        boolean removed = user.getAddresses().removeIf(addr -> addr.getLabel().equals(addressId));
        
        if (!removed) {
            throw new RuntimeException("Address not found");
        }
        
        // If deleted address was default, set another one as default
        if (user.getAddresses().stream().noneMatch(User.Address::getIsDefault) && !user.getAddresses().isEmpty()) {
            user.getAddresses().get(0).setIsDefault(true);
        }
        
        userRepository.save(user);
        log.info("Address deleted successfully for user email: {}", email);
    }
    
    public List<User.Address> getUserAddresses(String email) {
        log.info("Getting addresses for user email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return user.getAddresses() != null ? user.getAddresses() : List.of();
    }
    
    @Transactional
    public String uploadProfileImage(String email, MultipartFile file) throws Exception {
        log.info("Uploading profile image for user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete old profile image if exists
        if (user.getProfileImageUrl() != null) {
            try {
                fileUploadService.deleteProfileImage(user.getProfileImageUrl(), user.getId());
            } catch (Exception e) {
                log.warn("Failed to delete old profile image: {}", e.getMessage());
            }
        }
        
        // Upload new image
        String imageUrl = fileUploadService.uploadProfileImage(file, user.getId());
        
        // Update user profile
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
        
        log.info("Profile image uploaded successfully for user: {}", email);
        return imageUrl;
    }
    
    @Transactional
    public void deleteProfileImage(String email) throws Exception {
        log.info("Deleting profile image for user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getProfileImageUrl() != null) {
            fileUploadService.deleteProfileImage(user.getProfileImageUrl(), user.getId());
            user.setProfileImageUrl(null);
            userRepository.save(user);
            
            log.info("Profile image deleted successfully for user: {}", email);
        }
    }
    
    private UserProfileResponse mapToUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .isVerified(user.getIsVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .addresses(user.getAddresses())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
