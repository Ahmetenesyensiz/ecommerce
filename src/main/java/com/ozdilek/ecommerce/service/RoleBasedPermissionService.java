package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RoleBasedPermissionService {
    
    public boolean hasPermission(String role, String permission) {
        try {
            UserRole userRole = UserRole.fromCode(role);
            return userRole.hasPermission(permission);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role: {}", role);
            return false;
        }
    }
    
    public boolean hasAnyPermission(String role, String... permissions) {
        return Arrays.stream(permissions)
                .anyMatch(permission -> hasPermission(role, permission));
    }
    
    public boolean hasAllPermissions(String role, String... permissions) {
        return Arrays.stream(permissions)
                .allMatch(permission -> hasPermission(role, permission));
    }
    
    public List<String> getUserPermissions(String role) {
        return switch (role) {
            case "ADMIN" -> List.of(
                "user.manage", "user.view", "user.delete",
                "product.manage", "product.create", "product.update", "product.delete",
                "order.manage", "order.view", "order.update",
                "support.manage", "support.view",
                "inventory.manage", "inventory.view", "inventory.update",
                "analytics.view", "settings.manage"
            );
            case "SUPPORT" -> List.of(
                "user.view", "order.view", "order.update",
                "support.manage", "support.view",
                "product.view"
            );
            case "SELLER" -> List.of(
                "product.manage", "product.create", "product.update",
                "inventory.manage", "inventory.view", "inventory.update",
                "order.view", "order.update"
            );
            case "USER" -> List.of(
                "profile.manage", "profile.view",
                "cart.manage", "cart.view",
                "order.create", "order.view"
            );
            default -> List.of();
        };
    }
    
    public boolean canAccessAdminPanel(String role) {
        return hasPermission(role, "user.manage") || hasPermission(role, "product.manage");
    }
    
    public boolean canManageProducts(String role) {
        return hasPermission(role, "product.manage");
    }
    
    public boolean canManageUsers(String role) {
        return hasPermission(role, "user.manage");
    }
    
    public boolean canViewAnalytics(String role) {
        return hasPermission(role, "analytics.view");
    }
}
