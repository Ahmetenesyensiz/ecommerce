package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.dto.product.ProductCreateRequest;
import com.ozdilek.ecommerce.dto.product.ProductResponse;
import com.ozdilek.ecommerce.dto.product.ProductSearchRequest;
import com.ozdilek.ecommerce.dto.product.ProductUpdateRequest;
import com.ozdilek.ecommerce.model.Product;
import com.ozdilek.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Transactional(readOnly = true)
    // @Cacheable(value = "products", key = "#id") // Disabled for testing
    public ProductResponse findById(String id) {
        log.info("Finding product by id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return mapToResponse(product);
    }
    
    @Transactional(readOnly = true)
    // @Cacheable(value = "products", key = "#sku") // Disabled for testing
    public ProductResponse findBySku(String sku) {
        log.info("Finding product by sku: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with sku: " + sku));
        return mapToResponse(product);
    }
    
    @Transactional(readOnly = true)
    // @Cacheable(value = "products", key = "#slug") // Disabled for testing
    public ProductResponse findBySlug(String slug) {
        log.info("Finding product by slug: {}", slug);
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found with slug: " + slug));
        return mapToResponse(product);
    }
    
    @Transactional(readOnly = true)
    // @Cacheable(value = "product-search", key = "#searchRequest.toString()") // Disabled for testing
    public Page<ProductResponse> searchProducts(ProductSearchRequest searchRequest) {
        log.info("Searching products with request: {}", searchRequest);
        
        Pageable pageable = createPageable(searchRequest);
        Page<Product> products;
        
        if (searchRequest.getQ() != null && !searchRequest.getQ().trim().isEmpty()) {
            // Text search
            if (searchRequest.getCategories() != null && !searchRequest.getCategories().isEmpty() &&
                searchRequest.getMinPrice() != null && searchRequest.getMaxPrice() != null) {
                products = productRepository.findByTextSearchAndCategoriesAndPriceRangeAndAvailable(
                        searchRequest.getQ(),
                        searchRequest.getCategories(),
                        searchRequest.getMinPrice(),
                        searchRequest.getMaxPrice(),
                        pageable
                );
            } else if (searchRequest.getCategories() != null && !searchRequest.getCategories().isEmpty()) {
                products = productRepository.findByCategoriesAndAvailable(searchRequest.getCategories(), pageable);
            } else if (searchRequest.getMinPrice() != null && searchRequest.getMaxPrice() != null) {
                products = productRepository.findByPriceRangeAndAvailable(
                        searchRequest.getMinPrice(),
                        searchRequest.getMaxPrice(),
                        pageable
                );
            } else {
                products = productRepository.findByTextSearchAndAvailable(searchRequest.getQ(), pageable);
            }
        } else if (searchRequest.getCategories() != null && !searchRequest.getCategories().isEmpty() &&
                   searchRequest.getMinPrice() != null && searchRequest.getMaxPrice() != null) {
            products = productRepository.findByCategoriesAndPriceRangeAndAvailable(
                    searchRequest.getCategories(),
                    searchRequest.getMinPrice(),
                    searchRequest.getMaxPrice(),
                    pageable
            );
        } else if (searchRequest.getCategories() != null && !searchRequest.getCategories().isEmpty()) {
            products = productRepository.findByCategoriesAndAvailable(searchRequest.getCategories(), pageable);
        } else if (searchRequest.getMinPrice() != null && searchRequest.getMaxPrice() != null) {
            products = productRepository.findByPriceRangeAndAvailable(
                    searchRequest.getMinPrice(),
                    searchRequest.getMaxPrice(),
                    pageable
            );
        } else {
            products = productRepository.findAvailableProducts(pageable);
        }
        
        return products.map(this::mapToResponse);
    }
    
    @Transactional
    // @CacheEvict(value = {"products", "product-search"}, allEntries = true) // Disabled for testing
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating product with sku: {}", request.getSku());
        
        if (productRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Product already exists with sku: " + request.getSku());
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
                        .collect(Collectors.toList()) : null)
                .attributes(request.getAttributes())
                .stock(request.getStock())
                .available(request.getAvailable())
                .tags(request.getTags())
                .createdAt(LocalDateTime.now())
                .build();
        
        product = productRepository.save(product);
        log.info("Product created successfully with id: {}", product.getId());
        
        return mapToResponse(product);
    }
    
    @Transactional
    // @CacheEvict(value = {"products", "product-search"}, allEntries = true) // Disabled for testing
    public ProductResponse updateProduct(String id, ProductUpdateRequest request) {
        log.info("Updating product with id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        // Check slug uniqueness if changed
        if (request.getSlug() != null && !request.getSlug().equals(product.getSlug())) {
            if (productRepository.existsBySlug(request.getSlug())) {
                throw new RuntimeException("Product already exists with slug: " + request.getSlug());
            }
        }
        
        // Update fields
        if (request.getTitle() != null) product.setTitle(request.getTitle());
        if (request.getSlug() != null) product.setSlug(request.getSlug());
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
                    .collect(Collectors.toList()));
        }
        if (request.getAttributes() != null) product.setAttributes(request.getAttributes());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getAvailable() != null) product.setAvailable(request.getAvailable());
        if (request.getTags() != null) product.setTags(request.getTags());
        
        product.setUpdatedAt(LocalDateTime.now());
        product = productRepository.save(product);
        
        log.info("Product updated successfully with id: {}", product.getId());
        return mapToResponse(product);
    }
    
    @Transactional
    // @CacheEvict(value = {"products", "product-search"}, allEntries = true) // Disabled for testing
    public void deleteProduct(String id) {
        log.info("Deleting product with id: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        
        productRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }
    
    private Pageable createPageable(ProductSearchRequest searchRequest) {
        Sort sort = Sort.unsorted();
        
        if (searchRequest.getSort() != null) {
            switch (searchRequest.getSort()) {
                case "price-asc" -> sort = Sort.by("price").ascending();
                case "price-desc" -> sort = Sort.by("price").descending();
                case "name-asc" -> sort = Sort.by("title").ascending();
                case "name-desc" -> sort = Sort.by("title").descending();
                case "newest" -> sort = Sort.by("createdAt").descending();
            }
        }
        
        return PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
    }
    
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .title(product.getTitle())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .categories(product.getCategories())
                .images(product.getImages() != null ? product.getImages().stream()
                        .map(img -> ProductResponse.ProductImageResponse.builder()
                                .url(img.getUrl())
                                .alt(img.getAlt())
                                .sortOrder(img.getSortOrder())
                                .isPrimary(img.getIsPrimary())
                                .build())
                        .collect(Collectors.toList()) : null)
                .attributes(product.getAttributes())
                .stock(product.getStock())
                .available(product.getAvailable())
                .rating(product.getRating() != null ? ProductResponse.RatingResponse.builder()
                        .avg(product.getRating().getAvg())
                        .count(product.getRating().getCount())
                        .build() : null)
                .tags(product.getTags())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
