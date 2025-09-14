package com.ozdilek.ecommerce.controller;

import com.ozdilek.ecommerce.dto.cart.AddToCartRequest;
import com.ozdilek.ecommerce.dto.cart.CartResponse;
import com.ozdilek.ecommerce.dto.cart.MergeCartRequest;
import com.ozdilek.ecommerce.dto.cart.UpdateCartItemRequest;
import com.ozdilek.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
            Authentication authentication) {
        
        String userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = authentication.getName(); // email from JWT
        }
        
        log.info("Getting cart for userId: {}, sessionId: {}", userId, sessionId);
        CartResponse cart = cartService.getCart(userId, sessionId);
        return ResponseEntity.ok(cart);
    }
    
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
            Authentication authentication) {
        
        String userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = authentication.getName(); // email from JWT
        }
        
        log.info("Adding to cart - userId: {}, sessionId: {}, request: {}", userId, sessionId, request);
        CartResponse cart = cartService.addToCart(userId, sessionId, request);
        return ResponseEntity.ok(cart);
    }
    
    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateCartItem(
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
            Authentication authentication) {
        
        String userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = authentication.getName(); // email from JWT
        }
        
        log.info("Updating cart item - userId: {}, sessionId: {}, request: {}", userId, sessionId, request);
        CartResponse cart = cartService.updateCartItem(userId, sessionId, request);
        return ResponseEntity.ok(cart);
    }
    
    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeCart(
            @Valid @RequestBody MergeCartRequest request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        String userId = authentication.getName(); // email from JWT
        log.info("Merging cart for userId: {}, request: {}", userId, request);
        
        CartResponse cart = cartService.mergeCart(userId, request);
        return ResponseEntity.ok(cart);
    }
}
