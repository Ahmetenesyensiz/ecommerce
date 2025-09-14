package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.Product;
import com.ozdilek.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    
    /**
     * Atomically reserves stock for multiple products
     * This prevents race conditions when multiple orders are placed simultaneously
     */
    @Transactional
    public Map<String, Boolean> reserveStock(Map<String, Integer> productQuantities) {
        log.info("Reserving stock for products: {}", productQuantities);
        
        Map<String, Boolean> reservationResults = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();
            
            try {
                boolean reserved = reserveProductStock(productId, requestedQuantity);
                reservationResults.put(productId, reserved);
                
                if (!reserved) {
                    log.warn("Failed to reserve stock for product: {} (requested: {})", productId, requestedQuantity);
                }
            } catch (Exception e) {
                log.error("Error reserving stock for product: {}", productId, e);
                reservationResults.put(productId, false);
            }
        }
        
        return reservationResults;
    }
    
    /**
     * Atomically reserves stock for a single product using MongoDB's findAndModify
     * This ensures atomicity and prevents race conditions
     */
    private boolean reserveProductStock(String productId, Integer requestedQuantity) {
        Query query = new Query(Criteria.where("id").is(productId)
                .and("stock").gte(requestedQuantity)
                .and("available").is(true));
        
        Update update = new Update().inc("stock", -requestedQuantity);
        
        Product updatedProduct = mongoTemplate.findAndModify(query, update, Product.class);
        
        if (updatedProduct != null) {
            log.info("Successfully reserved {} units of product: {}", requestedQuantity, productId);
            return true;
        } else {
            log.warn("Insufficient stock for product: {} (requested: {})", productId, requestedQuantity);
            return false;
        }
    }
    
    /**
     * Releases reserved stock (used when order is cancelled)
     */
    @Transactional
    public boolean releaseStock(String productId, Integer quantity) {
        log.info("Releasing stock for product: {} (quantity: {})", productId, quantity);
        
        Query query = new Query(Criteria.where("id").is(productId));
        Update update = new Update().inc("stock", quantity);
        
        Product updatedProduct = mongoTemplate.findAndModify(query, update, Product.class);
        
        if (updatedProduct != null) {
            log.info("Successfully released {} units of product: {}", quantity, productId);
            return true;
        } else {
            log.warn("Failed to release stock for product: {}", productId);
            return false;
        }
    }
    
    /**
     * Checks if sufficient stock is available without reserving
     */
    public boolean checkStockAvailability(String productId, Integer requestedQuantity) {
        return productRepository.findById(productId)
                .map(product -> product.getStock() >= requestedQuantity && product.getAvailable())
                .orElse(false);
    }
    
    /**
     * Gets current stock level for a product
     */
    public Integer getCurrentStock(String productId) {
        return productRepository.findById(productId)
                .map(Product::getStock)
                .orElse(0);
    }
}
