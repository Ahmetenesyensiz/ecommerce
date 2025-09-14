package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.dto.cart.AddToCartRequest;
import com.ozdilek.ecommerce.dto.cart.CartResponse;
import com.ozdilek.ecommerce.dto.cart.MergeCartRequest;
import com.ozdilek.ecommerce.dto.cart.UpdateCartItemRequest;
import com.ozdilek.ecommerce.model.Cart;
import com.ozdilek.ecommerce.model.Cart.CartItem;
import com.ozdilek.ecommerce.model.Product;
import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.CartRepository;
import com.ozdilek.ecommerce.repository.ProductRepository;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    @Cacheable(value = "carts", key = "#userId != null ? #userId : #sessionId")
    public CartResponse getCart(String userId, String sessionId) {
        log.info("Getting cart for userId: {}, sessionId: {}", userId, sessionId);
        
        Cart cart = findOrCreateCart(userId, sessionId);
        return mapToCartResponse(cart);
    }
    
    @CacheEvict(value = "carts", key = "#userId != null ? #userId : #sessionId")
    @Transactional
    public CartResponse addToCart(String userId, String sessionId, AddToCartRequest request) {
        log.info("Adding to cart - userId: {}, sessionId: {}, productId: {}, qty: {}", 
                userId, sessionId, request.getProductId(), request.getQuantity());
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (!product.getAvailable() || product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Product not available or insufficient stock");
        }
        
        Cart cart = findOrCreateCart(userId, sessionId);
        
        // Check if product already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQty() + request.getQuantity();
            if (newQuantity > product.getStock()) {
                throw new RuntimeException("Insufficient stock");
            }
            item.setQty(newQuantity);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .productId(request.getProductId())
                    .sku(product.getSku())
                    .qty(request.getQuantity())
                    .priceSnapshot(product.getPrice())
                    .attributes((Map<String, Object>) (Map<?, ?>) request.getAttributes())
                    .build();
            
            cart.getItems().add(newItem);
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);
        
        return mapToCartResponse(cart);
    }
    
    @CacheEvict(value = "carts", key = "#userId != null ? #userId : #sessionId")
    @Transactional
    public CartResponse updateCartItem(String userId, String sessionId, UpdateCartItemRequest request) {
        log.info("Updating cart item - userId: {}, sessionId: {}, productId: {}, qty: {}", 
                userId, sessionId, request.getProductId(), request.getQuantity());
        
        Cart cart = findOrCreateCart(userId, sessionId);
        
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            
            if (request.getQuantity() <= 0) {
                // Remove item
                cart.getItems().remove(item);
            } else {
                // Update quantity
                Product product = productRepository.findById(request.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                
                if (request.getQuantity() > product.getStock()) {
                    throw new RuntimeException("Insufficient stock");
                }
                
                item.setQty(request.getQuantity());
            }
        } else {
            throw new RuntimeException("Cart item not found");
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);
        
        return mapToCartResponse(cart);
    }
    
    @CacheEvict(value = "carts", allEntries = true)
    @Transactional
    public CartResponse mergeCart(String userId, MergeCartRequest request) {
        log.info("Merging guest cart to user cart - userId: {}, guestSessionId: {}", 
                userId, request.getGuestSessionId());
        
        Cart userCart = findOrCreateCart(userId, null);
        Cart guestCart = cartRepository.findBySessionId(request.getGuestSessionId())
                .orElse(null);
        
        if (guestCart != null && !guestCart.getItems().isEmpty()) {
            for (CartItem guestItem : guestCart.getItems()) {
                // Check if product already exists in user cart
                Optional<CartItem> existingItem = userCart.getItems().stream()
                        .filter(item -> item.getProductId().equals(guestItem.getProductId()))
                        .findFirst();
                
                if (existingItem.isPresent()) {
                    // Merge quantities
                    CartItem item = existingItem.get();
                    item.setQty(item.getQty() + guestItem.getQty());
                } else {
                    // Add new item
                    userCart.getItems().add(guestItem);
                }
            }
            
            // Delete guest cart
            cartRepository.delete(guestCart);
            
            userCart.setUpdatedAt(LocalDateTime.now());
            userCart = cartRepository.save(userCart);
        }
        
        return mapToCartResponse(userCart);
    }
    
    private Cart findOrCreateCart(String userId, String sessionId) {
        Cart cart;
        
        if (userId != null) {
            cart = cartRepository.findByUserId(userId)
                    .orElse(Cart.builder()
                            .userId(userId)
                            .items(new ArrayList<>())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build());
        } else {
            cart = cartRepository.findBySessionId(sessionId)
                    .orElse(Cart.builder()
                            .sessionId(sessionId)
                            .items(new ArrayList<>())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build());
        }
        
        return cart;
    }
    
    private CartResponse mapToCartResponse(Cart cart) {
        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .sessionId(cart.getSessionId())
                .items(cart.getItems())
                .totalAmount(totalAmount)
                .itemCount(cart.getItems().size())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
