package com.ozdilek.ecommerce.model;

public enum UserRole {
    USER("USER", "Regular user with basic permissions"),
    ADMIN("ADMIN", "Administrator with full system access"),
    SUPPORT("SUPPORT", "Customer support representative"),
    SELLER("SELLER", "Product seller with inventory management");
    
    private final String code;
    private final String description;
    
    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }
    
    public boolean hasPermission(String permission) {
        return switch (this) {
            case ADMIN -> true; // Admin has all permissions
            case SUPPORT -> permission.matches("(user|order|support).*");
            case SELLER -> permission.matches("(product|inventory|order).*");
            case USER -> permission.matches("(profile|cart|order).*");
        };
    }
}
