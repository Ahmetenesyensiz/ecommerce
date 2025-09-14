package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.dto.product.ProductCreateRequest;
import com.ozdilek.ecommerce.dto.product.ProductUpdateRequest;
import com.ozdilek.ecommerce.model.Product;
import com.ozdilek.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductManagementService {
    
    private final ProductRepository productRepository;
    
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        log.info("Getting all products with pagination");
        return productRepository.findAll(pageable);
    }
    
    @Transactional(readOnly = true)
    public Product getProductById(String productId) {
        log.info("Getting product by id: {}", productId);
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }
    
    @Transactional
    public Product createProduct(ProductCreateRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());
        
        if (productRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Product already exists with SKU: " + request.getSku());
        }
        
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Product already exists with slug: " + request.getSlug());
        }
        
        Product product = Product.builder()
                .sku(request.getSku())
                .title(request.getTitle())
                .slug(request.getSlug())
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .categories(request.getCategories())
                .images(request.getImages() != null ? request.getImages().stream()
                        .map(img -> Product.ProductImage.builder()
                                .url(img.getUrl())
                                .alt(img.getAlt())
                                .sortOrder(img.getSortOrder())
                                .isPrimary(img.getIsPrimary())
                                .build())
                        .collect(java.util.stream.Collectors.toList()) : null)
                .attributes(request.getAttributes())
                .stock(request.getStock())
                .available(request.getAvailable())
                .tags(request.getTags())
                .createdAt(LocalDateTime.now())
                .build();
        
        product = productRepository.save(product);
        log.info("Product created successfully with ID: {}", product.getId());
        return product;
    }
    
    @Transactional
    public Product updateProduct(String productId, ProductUpdateRequest request) {
        log.info("Updating product: {}", productId);
        
        Product product = getProductById(productId);
        
        if (request.getTitle() != null) product.setTitle(request.getTitle());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCurrency() != null) product.setCurrency(request.getCurrency());
        if (request.getCategories() != null) product.setCategories(request.getCategories());
        if (request.getImages() != null) {
            product.setImages(request.getImages().stream()
                    .map(img -> Product.ProductImage.builder()
                            .url(img.getUrl())
                            .alt(img.getAlt())
                            .sortOrder(img.getSortOrder())
                            .isPrimary(img.getIsPrimary())
                            .build())
                    .collect(java.util.stream.Collectors.toList()));
        }
        if (request.getAttributes() != null) product.setAttributes(request.getAttributes());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getAvailable() != null) product.setAvailable(request.getAvailable());
        if (request.getTags() != null) product.setTags(request.getTags());
        
        product.setUpdatedAt(LocalDateTime.now());
        product = productRepository.save(product);
        
        log.info("Product updated successfully: {}", product.getSku());
        return product;
    }
    
    @Transactional
    public Product updateProductStock(String productId, Integer newStock) {
        log.info("Updating product stock: {} to {}", productId, newStock);
        
        Product product = getProductById(productId);
        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        
        product = productRepository.save(product);
        log.info("Product stock updated successfully: {} to {}", product.getSku(), newStock);
        return product;
    }
    
    @Transactional
    public Product updateProductPrice(String productId, BigDecimal newPrice) {
        log.info("Updating product price: {} to {}", productId, newPrice);
        
        Product product = getProductById(productId);
        product.setPrice(newPrice);
        product.setUpdatedAt(LocalDateTime.now());
        
        product = productRepository.save(product);
        log.info("Product price updated successfully: {} to {}", product.getSku(), newPrice);
        return product;
    }
    
    @Transactional
    public Product toggleProductAvailability(String productId) {
        log.info("Toggling product availability: {}", productId);
        
        Product product = getProductById(productId);
        product.setAvailable(!product.getAvailable());
        product.setUpdatedAt(LocalDateTime.now());
        
        product = productRepository.save(product);
        log.info("Product availability toggled: {} to {}", product.getSku(), product.getAvailable());
        return product;
    }
    
    @Transactional
    public void deleteProduct(String productId) {
        log.info("Deleting product: {}", productId);
        
        Product product = getProductById(productId);
        productRepository.delete(product);
        
        log.info("Product deleted successfully: {}", product.getSku());
    }
    
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(int threshold) {
        log.info("Getting products with stock below: {}", threshold);
        return productRepository.findAll().stream()
                .filter(product -> product.getStock() <= threshold && product.getAvailable())
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Product> getOutOfStockProducts() {
        log.info("Getting out of stock products");
        return productRepository.findAll().stream()
                .filter(product -> product.getStock() == 0 && product.getAvailable())
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Product> getUnavailableProducts() {
        log.info("Getting unavailable products");
        return productRepository.findAll().stream()
                .filter(product -> !product.getAvailable())
                .toList();
    }
    
    @Transactional(readOnly = true)
    public Map<String, Long> getProductStatistics() {
        log.info("Getting product statistics");
        
        List<Product> allProducts = productRepository.findAll();
        
        long totalProducts = allProducts.size();
        long availableProducts = allProducts.stream().filter(Product::getAvailable).count();
        long outOfStockProducts = allProducts.stream().filter(p -> p.getStock() == 0).count();
        long lowStockProducts = allProducts.stream().filter(p -> p.getStock() <= 10).count();
        
        return Map.of(
            "totalProducts", totalProducts,
            "availableProducts", availableProducts,
            "outOfStockProducts", outOfStockProducts,
            "lowStockProducts", lowStockProducts
        );
    }
    
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        String lowerQuery = query.toLowerCase();
        return productRepository.findAll().stream()
                .filter(product -> 
                    product.getTitle().toLowerCase().contains(lowerQuery) ||
                    product.getSku().toLowerCase().contains(lowerQuery) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerQuery))
                )
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String categoryId) {
        log.info("Getting products by category: {}", categoryId);
        return productRepository.findAll().stream()
                .filter(product -> product.getCategories() != null && product.getCategories().contains(categoryId))
                .toList();
    }
    
    @Transactional
    public Product addProductDiscount(String productId, BigDecimal discountPercentage) {
        log.info("Adding discount to product: {} - {}%", productId, discountPercentage);
        
        Product product = getProductById(productId);
        BigDecimal originalPrice = product.getPrice();
        BigDecimal discountAmount = originalPrice.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        BigDecimal newPrice = originalPrice.subtract(discountAmount);
        
        product.setPrice(newPrice);
        product.setUpdatedAt(LocalDateTime.now());
        
        // Add discount info to attributes
        if (product.getAttributes() == null) {
            product.setAttributes(Map.of());
        }
        Map<String, Object> attributes = product.getAttributes();
        attributes.put("originalPrice", originalPrice);
        attributes.put("discountPercentage", discountPercentage);
        attributes.put("discountAppliedAt", LocalDateTime.now());
        
        product = productRepository.save(product);
        log.info("Discount applied successfully to product: {}", product.getSku());
        return product;
    }
    
    @Transactional
    public Product removeProductDiscount(String productId) {
        log.info("Removing discount from product: {}", productId);
        
        Product product = getProductById(productId);
        
        if (product.getAttributes() != null && product.getAttributes().containsKey("originalPrice")) {
            BigDecimal originalPrice = (BigDecimal) product.getAttributes().get("originalPrice");
            product.setPrice(originalPrice);
            product.setUpdatedAt(LocalDateTime.now());
            
            // Remove discount info
            Map<String, Object> attributes = product.getAttributes();
            attributes.remove("originalPrice");
            attributes.remove("discountPercentage");
            attributes.remove("discountAppliedAt");
        }
        
        product = productRepository.save(product);
        log.info("Discount removed successfully from product: {}", product.getSku());
        return product;
    }
    
    @Transactional
    public void hardDeleteProduct(String productId) {
        log.info("Hard deleting product: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if product has any orders
        // In a real system, you might want to check order history
        // For now, we'll allow hard delete
        
        productRepository.delete(product);
        
        log.info("Product hard deleted: {}", productId);
    }
}
