package com.ozdilek.ecommerce.controller;

import com.ozdilek.ecommerce.dto.user.AddressRequest;
import com.ozdilek.ecommerce.dto.user.ProfileUpdateRequest;
import com.ozdilek.ecommerce.dto.user.UserProfileResponse;
import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.service.UserProfileService;
import com.ozdilek.ecommerce.service.UserDeletionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private final UserProfileService userProfileService;
    private final UserDeletionService userDeletionService;
    
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        log.info("Getting user profile for email: {}", email);
        
        UserProfileResponse profile = userProfileService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        log.info("Updating user profile for email: {}", email);
        
        UserProfileResponse profile = userProfileService.updateUserProfile(email, request);
        return ResponseEntity.ok(profile);
    }
    
    @GetMapping("/addresses")
    public ResponseEntity<List<User.Address>> getUserAddresses(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        log.info("Getting addresses for user email: {}", email);
        
        List<User.Address> addresses = userProfileService.getUserAddresses(email);
        return ResponseEntity.ok(addresses);
    }
    
    @PostMapping("/addresses")
    public ResponseEntity<Map<String, String>> addUserAddress(
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        log.info("Adding address for user email: {}", email);
        
        User.Address address = User.Address.builder()
                .label(request.getLabel())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.getIsDefault())
                .build();
        
        userProfileService.addUserAddress(email, address);
        
        return ResponseEntity.ok(Map.of(
            "message", "Address added successfully",
            "label", request.getLabel()
        ));
    }
    
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<Map<String, String>> updateUserAddress(
            @PathVariable String addressId,
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        log.info("Updating address {} for user email: {}", addressId, email);
        
        User.Address address = User.Address.builder()
                .label(request.getLabel())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.getIsDefault())
                .build();
        
        userProfileService.updateUserAddress(email, addressId, address);
        
        return ResponseEntity.ok(Map.of(
            "message", "Address updated successfully",
            "label", addressId
        ));
    }
    
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Map<String, String>> deleteUserAddress(
            @PathVariable String addressId,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        log.info("Deleting address {} for user email: {}", addressId, email);
        
        userProfileService.deleteUserAddress(email, addressId);
        
        return ResponseEntity.ok(Map.of(
            "message", "Address deleted successfully",
            "label", addressId
        ));
    }
    
    @PostMapping("/profile/image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        log.info("Uploading profile image for user: {}", email);
        
        try {
            String imageUrl = userProfileService.uploadProfileImage(email, file);
            return ResponseEntity.ok(Map.of(
                "message", "Profile image uploaded successfully",
                "imageUrl", imageUrl
            ));
        } catch (Exception e) {
            log.error("Profile image upload error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/profile/image")
    public ResponseEntity<Map<String, String>> deleteProfileImage(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        log.info("Deleting profile image for user: {}", email);
        
        try {
            userProfileService.deleteProfileImage(email);
            return ResponseEntity.ok(Map.of(
                "message", "Profile image deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Profile image deletion error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/account")
    public ResponseEntity<Map<String, String>> deleteAccount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String email = authentication.getName();
        log.info("Deleting account for user: {}", email);
        
        try {
            userDeletionService.softDeleteUser(email);
            return ResponseEntity.ok(Map.of(
                "message", "Account deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Account deletion error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
