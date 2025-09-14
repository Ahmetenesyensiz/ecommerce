package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.dto.order.CheckoutRequest;
import com.ozdilek.ecommerce.dto.order.OrderResponse;
import com.ozdilek.ecommerce.model.*;
import com.ozdilek.ecommerce.model.Order.OrderItem;
import com.ozdilek.ecommerce.model.Order.OrderStatus;
import com.ozdilek.ecommerce.model.Order.Address;
import com.ozdilek.ecommerce.model.Order.Payment;
import com.ozdilek.ecommerce.model.Order.OrderEvent;
import com.ozdilek.ecommerce.repository.CartRepository;
import com.ozdilek.ecommerce.repository.OrderRepository;
import com.ozdilek.ecommerce.repository.ProductRepository;
import com.ozdilek.ecommerce.repository.UserRepository;
import com.ozdilek.ecommerce.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    
    @Transactional
    public OrderResponse createOrder(String userId, CheckoutRequest request) {
        log.info("Creating order for userId: {}, cartId: {}", userId, request.getCartId());
        
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        if (!cart.getUserId().equals(user.getId())) {
            throw new RuntimeException("Cart does not belong to user");
        }
        
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Prepare stock reservation map
        Map<String, Integer> stockReservationMap = new HashMap<>();
        for (Cart.CartItem cartItem : cart.getItems()) {
            stockReservationMap.put(cartItem.getProductId(), cartItem.getQty());
        }
        
        // Atomically reserve stock for all products
        Map<String, Boolean> stockReservationResults = inventoryService.reserveStock(stockReservationMap);
        
        // Check if all stock reservations were successful
        for (Map.Entry<String, Boolean> entry : stockReservationResults.entrySet()) {
            if (!entry.getValue()) {
                throw new RuntimeException("Insufficient stock for product: " + entry.getKey());
            }
        }
        
        // Calculate totals
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (Cart.CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductId()));
            
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .sku(cartItem.getSku())
                    .title(product.getTitle())
                    .qty(cartItem.getQty())
                    .price(cartItem.getPriceSnapshot())
                    .attributes(cartItem.getAttributes())
                    .build();
            
            orderItems.add(orderItem);
            subtotal = subtotal.add(cartItem.getPriceSnapshot().multiply(BigDecimal.valueOf(cartItem.getQty())));
        }
        
        // Calculate shipping (mock calculation)
        BigDecimal shipping = calculateShipping(subtotal);
        BigDecimal total = subtotal.add(shipping);
        
        // Create order
        Order order = Order.builder()
                .userId(user.getId())
                .items(orderItems)
                .subtotal(subtotal)
                .shipping(shipping)
                .total(total)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .payment(Payment.builder()
                        .provider(request.getPaymentMethod().getProvider())
                        .transactionId("mock_" + UUID.randomUUID().toString())
                        .status("PENDING")
                        .build())
                .events(List.of(OrderEvent.builder()
                        .status(OrderStatus.PENDING)
                        .at(LocalDateTime.now())
                        .meta(null)
                        .build()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        order = orderRepository.save(order);
        
        // Clear cart (stock already reserved atomically)
        cartRepository.delete(cart);
        
        log.info("Order created successfully with ID: {}", order.getId());
        return mapToOrderResponse(order);
    }
    
    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        log.info("Getting orders for userId: {}", userId);
        
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return orders.map(this::mapToOrderResponse);
    }
    
    public OrderResponse getOrderById(String orderId, String userId) {
        log.info("Getting order by ID: {} for userId: {}", orderId, userId);
        
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("Order does not belong to user");
        }
        
        return mapToOrderResponse(order);
    }
    
    private BigDecimal calculateShipping(BigDecimal subtotal) {
        // Mock shipping calculation
        if (subtotal.compareTo(new BigDecimal("500")) >= 0) {
            return BigDecimal.ZERO; // Free shipping over 500 TL
        } else {
            return new BigDecimal("50"); // Standard shipping 50 TL
        }
    }
    
    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(order.getItems())
                .subtotal(order.getSubtotal())
                .shipping(order.getShipping())
                .total(order.getTotal())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .payment(order.getPayment())
                .events(order.getEvents())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
