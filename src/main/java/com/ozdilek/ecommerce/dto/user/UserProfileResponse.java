package com.ozdilek.ecommerce.dto.user;

import com.ozdilek.ecommerce.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    
    private String id;
    private String name;
    private String email;
    private String phone;
    private List<String> roles;
    private Boolean isVerified;
    private String profileImageUrl;
    private List<User.Address> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
