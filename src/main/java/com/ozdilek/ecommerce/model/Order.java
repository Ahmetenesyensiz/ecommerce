package com.ozdilek.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;
    
    @NotNull
    private String userId;
    
    private List<OrderItem> items;
    
    @NotNull
    private BigDecimal subtotal;
    
    private BigDecimal shipping;
    
    @NotNull
    private BigDecimal total;
    
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    private Address shippingAddress;
    
    private Address billingAddress;
    
    private Payment payment;
    
    private List<OrderEvent> events;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public enum OrderStatus {
        PENDING, PAID, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private String sku;
        private String title;
        private Integer qty;
        private BigDecimal price;
        private Map<String, Object> attributes;
    }
    
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
        private String phone;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payment {
        private String provider;
        private String transactionId;
        private String status;
        private BigDecimal amount;
        private String currency;
        private LocalDateTime processedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderEvent {
        private OrderStatus status;
        private LocalDateTime at;
        private String message;
        private Map<String, Object> meta;
    }
}
