package com.ozdilek.ecommerce.dto.order;

import com.ozdilek.ecommerce.model.Order;
import com.ozdilek.ecommerce.model.Order.OrderItem;
import com.ozdilek.ecommerce.model.Order.OrderStatus;
import com.ozdilek.ecommerce.model.Order.Address;
import com.ozdilek.ecommerce.model.Order.Payment;
import com.ozdilek.ecommerce.model.Order.OrderEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private String id;
    private String userId;
    private List<OrderItem> items;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal total;
    private OrderStatus status;
    private Address shippingAddress;
    private Address billingAddress;
    private Payment payment;
    private List<OrderEvent> events;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
