package com.ozdilek.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @Email
    @NotBlank
    @Indexed(unique = true)
    private String email;
    
    @NotBlank
    private String passwordHash;
    
    @NotBlank
    private String name;
    
    private String phone;
    
    @Builder.Default
    private List<String> roles = List.of("USER");
    
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime lastLoginAt;
    
    @Builder.Default
    private Boolean isVerified = false;
    
    private String profileImageUrl;
    
    @Builder.Default
    private Boolean deleted = false;
    
    private LocalDateTime deletedAt;
    
    private Object metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String label;
        private String line1;
        private String line2;
        private String city;
        private String postalCode;
        private String country;
        private Boolean isDefault;
    }
}
