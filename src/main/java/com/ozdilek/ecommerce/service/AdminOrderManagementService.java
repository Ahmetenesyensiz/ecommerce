package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.Order;
import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.OrderRepository;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderManagementService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        log.info("Getting all orders with pagination");
        return orderRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public Order getOrderById(String orderId) {
        log.info("Getting order by id: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }
    
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        log.info("Getting orders by status: {}", status);
        return orderRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting orders between {} and {}", startDate, endDate);
        return orderRepository.findAll().stream()
                .filter(order -> order.getCreatedAt().isAfter(startDate) && order.getCreatedAt().isBefore(endDate))
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(String userId) {
        log.info("Getting orders for user: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Transactional
    public Order updateOrderStatus(String orderId, Order.OrderStatus newStatus, String adminNote) {
        log.info("Updating order status: {} to {}", orderId, newStatus);
        
        Order order = getOrderById(orderId);
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Add status change event
        if (order.getEvents() == null) {
            order.setEvents(List.of());
        }
        
        Order.OrderEvent statusChangeEvent = Order.OrderEvent.builder()
                .status(newStatus)
                .at(LocalDateTime.now())
                .meta(Map.of(
                    "previousStatus", oldStatus.toString(),
                    "adminNote", adminNote != null ? adminNote : "",
                    "changedBy", "ADMIN"
                ))
                .build();
        
        order.getEvents().add(statusChangeEvent);
        
        order = orderRepository.save(order);
        log.info("Order status updated successfully: {} from {} to {}", orderId, oldStatus, newStatus);
        return order;
    }
    
    @Transactional
    public Order cancelOrder(String orderId, String reason) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);
        
        Order order = getOrderById(orderId);
        
        if (order.getStatus() == Order.OrderStatus.DELIVERED || order.getStatus() == Order.OrderStatus.REFUNDED) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }
        
        return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED, "Cancelled by admin. Reason: " + reason);
    }
    
    @Transactional
    public Order refundOrder(String orderId, String reason) {
        log.info("Refunding order: {} with reason: {}", orderId, reason);
        
        Order order = getOrderById(orderId);
        
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Can only refund delivered orders. Current status: " + order.getStatus());
        }
        
        return updateOrderStatus(orderId, Order.OrderStatus.REFUNDED, "Refunded by admin. Reason: " + reason);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderStatistics() {
        log.info("Getting order statistics");
        
        List<Order> allOrders = orderRepository.findAll();
        
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.PENDING).count();
        long confirmedOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.CONFIRMED).count();
        long shippedOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.SHIPPED).count();
        long deliveredOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED).count();
        long cancelledOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED).count();
        long refundedOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.REFUNDED).count();
        
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageOrderValue = totalOrders > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
        
        return Map.of(
            "totalOrders", totalOrders,
            "pendingOrders", pendingOrders,
            "confirmedOrders", confirmedOrders,
            "shippedOrders", shippedOrders,
            "deliveredOrders", deliveredOrders,
            "cancelledOrders", cancelledOrders,
            "refundedOrders", refundedOrders,
            "totalRevenue", totalRevenue,
            "averageOrderValue", averageOrderValue
        );
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getDailySales(LocalDateTime date) {
        log.info("Getting daily sales for: {}", date);
        
        LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59);
        
        List<Order> dailyOrders = getOrdersByDateRange(startOfDay, endOfDay);
        
        long totalOrders = dailyOrders.size();
        long deliveredOrders = dailyOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .count();
        
        BigDecimal totalRevenue = dailyOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Map.of(
            "date", date.toLocalDate(),
            "totalOrders", totalOrders,
            "deliveredOrders", deliveredOrders,
            "totalRevenue", totalRevenue
        );
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        log.info("Getting top selling products, limit: {}", limit);
        
        List<Order> deliveredOrders = getOrdersByStatus(Order.OrderStatus.DELIVERED);
        
        Map<String, Integer> productSales = new java.util.HashMap<>();
        Map<String, String> productNames = new java.util.HashMap<>();
        
        for (Order order : deliveredOrders) {
            for (Order.OrderItem item : order.getItems()) {
                String productId = item.getProductId();
                productSales.merge(productId, item.getQty(), Integer::sum);
                productNames.put(productId, item.getTitle());
            }
        }
        
        return productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("productId", entry.getKey());
                    result.put("productName", productNames.get(entry.getKey()));
                    result.put("totalSold", entry.getValue());
                    return result;
                })
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Order> searchOrders(String query) {
        String lowerQuery = query.toLowerCase();
        return orderRepository.findAll().stream()
                .filter(order -> 
                    order.getId().toLowerCase().contains(lowerQuery) ||
                    order.getUserId().toLowerCase().contains(lowerQuery) ||
                    (order.getShippingAddress() != null && 
                     (order.getShippingAddress().getLine1().toLowerCase().contains(lowerQuery) ||
                      order.getShippingAddress().getCity().toLowerCase().contains(lowerQuery)))
                )
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentOrders(int limit) {
        log.info("Getting recent orders, limit: {}", limit);
        
        return orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit)
                .map(order -> {
                    User user = userRepository.findById(order.getUserId()).orElse(null);
                    Map<String, Object> result = new HashMap<>();
                    result.put("orderId", order.getId());
                    result.put("userName", user != null ? user.getName() : "Unknown");
                    result.put("userEmail", user != null ? user.getEmail() : "Unknown");
                    result.put("total", order.getTotal());
                    result.put("status", order.getStatus());
                    result.put("createdAt", order.getCreatedAt());
                    result.put("itemCount", order.getItems().size());
                    return result;
                })
                .toList();
    }
}
