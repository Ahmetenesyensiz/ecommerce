package com.ozdilek.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "phone_verifications")
public class PhoneVerification {
    
    @Id
    private String id;
    
    @NotBlank
    @Indexed(unique = true)
    private String phone;
    
    @NotBlank
    private String otp;
    
    @NotNull
    private LocalDateTime createdAt;
    
    @NotNull
    private LocalDateTime expiresAt;
    
    @Builder.Default
    private Integer attempts = 0;
    
    @Builder.Default
    private Integer maxAttempts = 3;
    
    @Builder.Default
    private Boolean verified = false;
    
    private String userId;
}
