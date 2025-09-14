package com.ozdilek.ecommerce.dto.cart;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MergeCartRequest {
    
    @NotBlank(message = "Guest session ID is required")
    private String guestSessionId;
}
