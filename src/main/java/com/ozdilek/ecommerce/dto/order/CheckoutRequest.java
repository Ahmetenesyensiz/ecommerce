package com.ozdilek.ecommerce.dto.order;

import com.ozdilek.ecommerce.model.Order.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    
    @NotBlank(message = "Cart ID is required")
    private String cartId;
    
    @NotNull(message = "Shipping address is required")
    private Address shippingAddress;
    
    @NotNull(message = "Billing address is required")
    private Address billingAddress;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethodRequest paymentMethod;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodRequest {
        @NotBlank(message = "Payment provider is required")
        private String provider; // "stripe", "iyzico", etc.
        
        private String token; // Payment token from frontend
    }
}
