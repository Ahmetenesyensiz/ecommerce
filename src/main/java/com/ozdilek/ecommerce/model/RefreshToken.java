package com.ozdilek.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshToken {
    
    @Id
    private String id;
    
    @NotNull
    private String userId;
    
    @NotBlank
    private String tokenHash;
    
    @NotNull
    private LocalDateTime issuedAt;
    
    @NotNull
    private LocalDateTime expiresAt;
    
    private String ip;
    
    private String userAgent;
    
    @Builder.Default
    private Boolean revoked = false;
}
