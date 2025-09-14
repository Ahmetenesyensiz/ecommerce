package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.Order;
import com.ozdilek.ecommerce.model.Product;
import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.OrderRepository;
import com.ozdilek.ecommerce.repository.ProductRepository;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAnalyticsService {
    
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardOverview() {
        log.info("Getting dashboard overview");
        
        // User Statistics
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(u -> !u.getDeleted()).count();
        long verifiedUsers = allUsers.stream().filter(u -> u.getIsVerified() && !u.getDeleted()).count();
        long newUsersThisMonth = allUsers.stream()
                .filter(u -> u.getCreatedAt().isAfter(LocalDateTime.now().minusMonths(1)))
                .count();
        
        // Product Statistics
        List<Product> allProducts = productRepository.findAll();
        long totalProducts = allProducts.size();
        long availableProducts = allProducts.stream().filter(Product::getAvailable).count();
        long outOfStockProducts = allProducts.stream().filter(p -> p.getStock() == 0).count();
        long lowStockProducts = allProducts.stream().filter(p -> p.getStock() <= 10 && p.getStock() > 0).count();
        
        // Order Statistics
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.PENDING).count();
        long deliveredOrders = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED).count();
        
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal thisMonthRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                .filter(o -> o.getCreatedAt().isAfter(LocalDateTime.now().minusMonths(1)))
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Map.of(
            "users", Map.of(
                "total", totalUsers,
                "active", activeUsers,
                "verified", verifiedUsers,
                "newThisMonth", newUsersThisMonth
            ),
            "products", Map.of(
                "total", totalProducts,
                "available", availableProducts,
                "outOfStock", outOfStockProducts,
                "lowStock", lowStockProducts
            ),
            "orders", Map.of(
                "total", totalOrders,
                "pending", pendingOrders,
                "delivered", deliveredOrders,
                "totalRevenue", totalRevenue,
                "thisMonthRevenue", thisMonthRevenue
            )
        );
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getSalesReport(LocalDate startDate, LocalDate endDate) {
        log.info("Getting sales report from {} to {}", startDate, endDate);
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<Order> ordersInRange = orderRepository.findAll().stream()
                .filter(order -> order.getCreatedAt().isAfter(start) && order.getCreatedAt().isBefore(end))
                .collect(Collectors.toList());
        
        // Daily breakdown
        Map<LocalDate, BigDecimal> dailyRevenue = new LinkedHashMap<>();
        Map<LocalDate, Long> dailyOrders = new LinkedHashMap<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dailyRevenue.put(currentDate, BigDecimal.ZERO);
            dailyOrders.put(currentDate, 0L);
            currentDate = currentDate.plusDays(1);
        }
        
        for (Order order : ordersInRange) {
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                dailyRevenue.merge(orderDate, order.getTotal(), BigDecimal::add);
            }
            dailyOrders.merge(orderDate, 1L, Long::sum);
        }
        
        // Summary
        BigDecimal totalRevenue = dailyRevenue.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long totalOrders = dailyOrders.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        
        BigDecimal averageOrderValue = totalOrders > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
        
        return Map.of(
            "period", Map.of(
                "startDate", startDate,
                "endDate", endDate
            ),
            "summary", Map.of(
                "totalRevenue", totalRevenue,
                "totalOrders", totalOrders,
                "averageOrderValue", averageOrderValue
            ),
            "dailyBreakdown", Map.of(
                "revenue", dailyRevenue,
                "orders", dailyOrders
            )
        );
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopSellingProducts(int limit, LocalDate startDate, LocalDate endDate) {
        log.info("Getting top selling products, limit: {}, from {} to {}", limit, startDate, endDate);
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<Order> ordersInRange = orderRepository.findAll().stream()
                .filter(order -> order.getCreatedAt().isAfter(start) && order.getCreatedAt().isBefore(end))
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .collect(Collectors.toList());
        
        Map<String, Integer> productSales = new HashMap<>();
        Map<String, String> productNames = new HashMap<>();
        Map<String, BigDecimal> productRevenue = new HashMap<>();
        
        for (Order order : ordersInRange) {
            for (Order.OrderItem item : order.getItems()) {
                String productId = item.getProductId();
                productSales.merge(productId, item.getQty(), Integer::sum);
                productNames.put(productId, item.getTitle());
                
                BigDecimal itemRevenue = item.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
                productRevenue.merge(productId, itemRevenue, BigDecimal::add);
            }
        }
        
        return productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    String productId = entry.getKey();
                    Map<String, Object> result = new HashMap<>();
                    result.put("productId", productId);
                    result.put("productName", productNames.get(productId));
                    result.put("totalSold", entry.getValue());
                    result.put("totalRevenue", productRevenue.get(productId));
                    return result;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserGrowthReport(LocalDate startDate, LocalDate endDate) {
        log.info("Getting user growth report from {} to {}", startDate, endDate);
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<User> usersInRange = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(start) && user.getCreatedAt().isBefore(end))
                .collect(Collectors.toList());
        
        // Daily breakdown
        Map<LocalDate, Long> dailyNewUsers = new LinkedHashMap<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dailyNewUsers.put(currentDate, 0L);
            currentDate = currentDate.plusDays(1);
        }
        
        for (User user : usersInRange) {
            LocalDate userDate = user.getCreatedAt().toLocalDate();
            dailyNewUsers.merge(userDate, 1L, Long::sum);
        }
        
        // Cumulative growth
        Map<LocalDate, Long> cumulativeUsers = new LinkedHashMap<>();
        long cumulative = 0;
        currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            cumulative += dailyNewUsers.get(currentDate);
            cumulativeUsers.put(currentDate, cumulative);
            currentDate = currentDate.plusDays(1);
        }
        
        return Arrays.asList(
            Map.of(
                "type", "daily",
                "data", dailyNewUsers
            ),
            Map.of(
                "type", "cumulative",
                "data", cumulativeUsers
            )
        );
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getAbandonedCartAnalysis() {
        log.info("Getting abandoned cart analysis");
        
        // This would need cart data - for now return mock data
        // In a real implementation, you'd analyze carts that were created but never converted to orders
        
        return Map.of(
            "abandonedCarts", 45,
            "totalCarts", 120,
            "abandonmentRate", 37.5,
            "potentialRevenue", BigDecimal.valueOf(2500.00),
            "topAbandonedProducts", Arrays.asList(
                "Laptop Pro 15\"",
                "Wireless Headphones",
                "Smart Watch Series 5"
            )
        );
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getInventoryAlerts() {
        log.info("Getting inventory alerts");
        
        List<Product> allProducts = productRepository.findAll();
        
        List<Product> outOfStock = allProducts.stream()
                .filter(p -> p.getStock() == 0 && p.getAvailable())
                .collect(Collectors.toList());
        
        List<Product> lowStock = allProducts.stream()
                .filter(p -> p.getStock() > 0 && p.getStock() <= 10 && p.getAvailable())
                .collect(Collectors.toList());
        
        List<Product> discontinued = allProducts.stream()
                .filter(p -> !p.getAvailable())
                .collect(Collectors.toList());
        
        return Map.of(
            "outOfStock", outOfStock.stream().map(p -> Map.of(
                "id", p.getId(),
                "sku", p.getSku(),
                "title", p.getTitle(),
                "stock", p.getStock()
            )).collect(Collectors.toList()),
            "lowStock", lowStock.stream().map(p -> Map.of(
                "id", p.getId(),
                "sku", p.getSku(),
                "title", p.getTitle(),
                "stock", p.getStock()
            )).collect(Collectors.toList()),
            "discontinued", discontinued.stream().map(p -> Map.of(
                "id", p.getId(),
                "sku", p.getSku(),
                "title", p.getTitle(),
                "stock", p.getStock()
            )).collect(Collectors.toList())
        );
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentActivity(int limit) {
        log.info("Getting recent activity, limit: {}", limit);
        
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Recent orders
        List<Order> recentOrders = orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit / 2)
                .collect(Collectors.toList());
        
        for (Order order : recentOrders) {
            activities.add(Map.of(
                "type", "order",
                "description", "New order created: " + order.getId(),
                "timestamp", order.getCreatedAt(),
                "details", Map.of(
                    "orderId", order.getId(),
                    "total", order.getTotal(),
                    "status", order.getStatus()
                )
            ));
        }
        
        // Recent users
        List<User> recentUsers = userRepository.findAll().stream()
                .filter(u -> !u.getDeleted())
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .limit(limit / 2)
                .collect(Collectors.toList());
        
        for (User user : recentUsers) {
            activities.add(Map.of(
                "type", "user",
                "description", "New user registered: " + user.getName(),
                "timestamp", user.getCreatedAt(),
                "details", Map.of(
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "roles", user.getRoles()
                )
            ));
        }
        
        // Sort by timestamp and limit
        return activities.stream()
                .sorted((a1, a2) -> ((LocalDateTime) a2.get("timestamp")).compareTo((LocalDateTime) a1.get("timestamp")))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
