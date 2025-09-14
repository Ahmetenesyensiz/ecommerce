package com.ozdilek.ecommerce.controller;

import com.ozdilek.ecommerce.dto.product.ProductCreateRequest;
import com.ozdilek.ecommerce.dto.product.ProductResponse;
import com.ozdilek.ecommerce.dto.product.ProductSearchRequest;
import com.ozdilek.ecommerce.dto.product.ProductUpdateRequest;
import com.ozdilek.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean available) {
        
        log.info("Search products endpoint called with params: q={}, categories={}, minPrice={}, maxPrice={}, page={}, size={}, sort={}, available={}", 
                q, categories, minPrice, maxPrice, page, size, sort, available);
        
        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .q(q)
                .categories(categories != null ? java.util.List.of(categories.split(",")) : null)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .page(page)
                .size(size)
                .sort(sort)
                .available(available)
                .build();
        
        Page<ProductResponse> products = productService.searchProducts(searchRequest);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        log.info("Get product by id endpoint called with id: {}", id);
        ProductResponse product = productService.findById(id);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        log.info("Get product by sku endpoint called with sku: {}", sku);
        ProductResponse product = productService.findBySku(sku);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        log.info("Get product by slug endpoint called with slug: {}", slug);
        ProductResponse product = productService.findBySlug(slug);
        return ResponseEntity.ok(product);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        log.info("Create product endpoint called with sku: {}", request.getSku());
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("Update product endpoint called with id: {}", id);
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable String id) {
        log.info("Delete product endpoint called with id: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }
}
