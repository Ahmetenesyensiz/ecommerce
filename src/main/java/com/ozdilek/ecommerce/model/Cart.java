package com.ozdilek.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {
    
    @Id
    private String id;
    
    private String userId; // null for guest carts
    
    private String sessionId; // for guest carts
    
    @Builder.Default
    private List<CartItem> items = List.of();
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        @NotNull
        private String productId;
        
        @NotBlank
        private String sku;
        
        @NotNull
        @Positive
        private Integer qty;
        
        @NotNull
        private BigDecimal priceSnapshot; // price when added to cart
        
        private Map<String, Object> attributes; // color, size etc.
    }
}
