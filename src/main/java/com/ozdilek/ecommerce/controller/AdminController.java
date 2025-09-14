package com.ozdilek.ecommerce.controller;

import com.ozdilek.ecommerce.dto.product.ProductCreateRequest;
import com.ozdilek.ecommerce.dto.product.ProductUpdateRequest;
import com.ozdilek.ecommerce.model.Order;
import com.ozdilek.ecommerce.model.Product;
import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    
    private final AdminUserManagementService adminUserService;
    private final AdminProductManagementService adminProductService;
    private final AdminOrderManagementService adminOrderService;
    private final AdminAnalyticsService adminAnalyticsService;
    private final RoleBasedPermissionService permissionService;
    
    // ==================== USER MANAGEMENT ====================
    
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = adminUserService.getAllUsers(pageable);
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        User user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/users/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam String query,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        List<User> users = adminUserService.searchUsers(query);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/by-role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        List<User> users = adminUserService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<User> updateUserRoles(
            @PathVariable String userId,
            @RequestBody List<String> newRoles,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        User user = adminUserService.updateUserRole(userId, newRoles);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<User> banUser(
            @PathVariable String userId,
            @RequestParam String reason,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        User user = adminUserService.banUser(userId, reason);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<User> unbanUser(@PathVariable String userId, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        User user = adminUserService.unbanUser(userId);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> hardDeleteUser(@PathVariable String userId, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        adminUserService.hardDeleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
    
    // ==================== PRODUCT MANAGEMENT ====================
    
    @GetMapping("/products")
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = adminProductService.getAllProducts(pageable);
        
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable String productId, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Product product = adminProductService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
    
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Product product = adminProductService.createProduct(request);
        return ResponseEntity.ok(product);
    }
    
    @PutMapping("/products/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody ProductUpdateRequest request,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Product product = adminProductService.updateProduct(productId, request);
        return ResponseEntity.ok(product);
    }
    
    @PutMapping("/products/{productId}/stock")
    public ResponseEntity<Product> updateProductStock(
            @PathVariable String productId,
            @RequestParam Integer stock,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Product product = adminProductService.updateProductStock(productId, stock);
        return ResponseEntity.ok(product);
    }
    
    @PutMapping("/products/{productId}/price")
    public ResponseEntity<Product> updateProductPrice(
            @PathVariable String productId,
            @RequestParam BigDecimal price,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Product product = adminProductService.updateProductPrice(productId, price);
        return ResponseEntity.ok(product);
    }
    
    @PutMapping("/products/{productId}/toggle-availability")
    public ResponseEntity<Product> toggleProductAvailability(@PathVariable String productId, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Product product = adminProductService.toggleProductAvailability(productId);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable String productId, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        adminProductService.deleteProduct(productId);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }
    
    @DeleteMapping("/products/{productId}/hard")
    public ResponseEntity<Map<String, String>> hardDeleteProduct(@PathVariable String productId, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        adminProductService.hardDeleteProduct(productId);
        return ResponseEntity.ok(Map.of("message", "Product permanently deleted"));
    }
    
    @GetMapping("/products/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        List<Product> products = adminProductService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/out-of-stock")
    public ResponseEntity<List<Product>> getOutOfStockProducts(Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        List<Product> products = adminProductService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam String query,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        List<Product> products = adminProductService.searchProducts(query);
        return ResponseEntity.ok(products);
    }
    
    // ==================== ORDER MANAGEMENT ====================
    
    @GetMapping("/orders")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = adminOrderService.getAllOrders(pageable);
        
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Order order = adminOrderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/orders/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status, Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        List<Order> orders = adminOrderService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status,
            @RequestParam(required = false) String note,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        Order order = adminOrderService.updateOrderStatus(orderId, newStatus, note);
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String reason,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Order order = adminOrderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/orders/{orderId}/refund")
    public ResponseEntity<Order> refundOrder(
            @PathVariable String orderId,
            @RequestParam String reason,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Order order = adminOrderService.refundOrder(orderId, reason);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/orders/search")
    public ResponseEntity<List<Order>> searchOrders(
            @RequestParam String query,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        List<Order> orders = adminOrderService.searchOrders(query);
        return ResponseEntity.ok(orders);
    }
    
    // ==================== ANALYTICS & DASHBOARD ====================
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardOverview(Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Map<String, Object> overview = adminAnalyticsService.getDashboardOverview();
        return ResponseEntity.ok(overview);
    }
    
    @GetMapping("/analytics/sales")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        Map<String, Object> report = adminAnalyticsService.getSalesReport(start, end);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/analytics/top-products")
    public ResponseEntity<List<Map<String, Object>>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<Map<String, Object>> products = adminAnalyticsService.getTopSellingProducts(limit, start, end);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/analytics/user-growth")
    public ResponseEntity<List<Map<String, Object>>> getUserGrowthReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<Map<String, Object>> report = adminAnalyticsService.getUserGrowthReport(start, end);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/analytics/inventory-alerts")
    public ResponseEntity<Map<String, Object>> getInventoryAlerts(Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        Map<String, Object> alerts = adminAnalyticsService.getInventoryAlerts();
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/analytics/recent-activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        List<Map<String, Object>> activity = adminAnalyticsService.getRecentActivity(limit);
        return ResponseEntity.ok(activity);
    }
    
    // ==================== PERMISSIONS ====================
    
    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Object>> getUserPermissions(Authentication authentication) {
        if (!isAdminAuthenticated(authentication)) {
            return ResponseEntity.status(401).build();
        }
        
        String userRole = getUserRole(authentication);
        List<String> permissions = permissionService.getUserPermissions(userRole);
        
        return ResponseEntity.ok(Map.of(
            "role", userRole,
            "permissions", permissions,
            "canAccessAdminPanel", permissionService.canAccessAdminPanel(userRole),
            "canManageProducts", permissionService.canManageProducts(userRole),
            "canManageUsers", permissionService.canManageUsers(userRole),
            "canViewAnalytics", permissionService.canViewAnalytics(userRole)
        ));
    }
    
    // ==================== UTILITY METHODS ====================
    
    private boolean isAdminAuthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String userRole = getUserRole(authentication);
        return permissionService.canAccessAdminPanel(userRole);
    }
    
    private String getUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("USER");
    }
}
