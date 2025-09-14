package com.ozdilek.ecommerce.dto.product;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    
    private String title;
    
    private String slug;
    
    private String description;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    private String currency;
    
    private List<String> categories;
    
    private List<ProductImageRequest> images;
    
    private Map<String, Object> attributes;
    
    private Integer stock;
    
    private Boolean available;
    
    private List<String> tags;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageRequest {
        private String url;
        private String alt;
        private Integer sortOrder;
        private Boolean isPrimary;
    }
}
