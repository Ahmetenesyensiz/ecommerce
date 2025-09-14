package com.ozdilek.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class SecurityHeadersFilter extends OncePerRequestFilter {
    
    private static final List<String> SECURITY_HEADERS = Arrays.asList(
        "X-Content-Type-Options",
        "X-Frame-Options",
        "X-XSS-Protection",
        "Strict-Transport-Security",
        "Content-Security-Policy",
        "Referrer-Policy",
        "Permissions-Policy"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Add security headers
        addSecurityHeaders(response);
        
        // Log suspicious activity
        logSuspiciousActivity(request);
        
        filterChain.doFilter(request, response);
    }
    
    private void addSecurityHeaders(HttpServletResponse response) {
        // Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        
        // Enable XSS protection
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Force HTTPS (only in production)
        // response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        
        // Content Security Policy
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self'; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'");
        
        // Referrer Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions Policy
        response.setHeader("Permissions-Policy", 
            "geolocation=(), " +
            "microphone=(), " +
            "camera=(), " +
            "payment=(), " +
            "usb=(), " +
            "magnetometer=(), " +
            "accelerometer=(), " +
            "gyroscope=()");
        
        // Remove server header
        response.setHeader("Server", "");
        
        // Cache control for sensitive endpoints
        String requestUri = ((HttpServletRequest) response).getRequestURI();
        if (requestUri.contains("/api/auth") || requestUri.contains("/api/admin")) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
    }
    
    private void logSuspiciousActivity(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        String requestUri = request.getRequestURI();
        
        // Check for suspicious patterns
        if (isSuspiciousUserAgent(userAgent)) {
            log.warn("Suspicious User-Agent detected from IP {}: {}", clientIp, userAgent);
        }
        
        if (isSuspiciousRequest(requestUri)) {
            log.warn("Suspicious request detected from IP {}: {}", clientIp, requestUri);
        }
        
        // Log failed authentication attempts
        if (requestUri.contains("/api/auth/login") && request.getMethod().equals("POST")) {
            log.info("Login attempt from IP: {} User-Agent: {}", clientIp, userAgent);
        }
    }
    
    private boolean isSuspiciousUserAgent(String userAgent) {
        if (userAgent == null) return true;
        
        String[] suspiciousPatterns = {
            "bot", "crawler", "spider", "scraper", "curl", "wget", 
            "python", "java", "php", "sqlmap", "nikto"
        };
        
        String lowerUserAgent = userAgent.toLowerCase();
        return Arrays.stream(suspiciousPatterns)
                .anyMatch(pattern -> lowerUserAgent.contains(pattern));
    }
    
    private boolean isSuspiciousRequest(String requestUri) {
        String[] suspiciousPatterns = {
            "../", "..\\", "admin", "config", "backup", "test", 
            "php", "asp", "jsp", "sql", "script"
        };
        
        String lowerUri = requestUri.toLowerCase();
        return Arrays.stream(suspiciousPatterns)
                .anyMatch(pattern -> lowerUri.contains(pattern));
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
