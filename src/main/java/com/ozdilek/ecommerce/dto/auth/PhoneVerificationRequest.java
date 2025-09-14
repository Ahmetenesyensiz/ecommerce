package com.ozdilek.ecommerce.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneVerificationRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+90[0-9]{10}$", message = "Phone number must be in format +90XXXXXXXXXX")
    private String phone;
    
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    private String otp;
}
