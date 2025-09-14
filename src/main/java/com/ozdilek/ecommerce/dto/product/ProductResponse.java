package com.ozdilek.ecommerce.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private String id;
    private String sku;
    private String title;
    private String slug;
    private String description;
    private BigDecimal price;
    private String currency;
    private List<String> categories;
    private List<ProductImageResponse> images;
    private Map<String, Object> attributes;
    private Integer stock;
    private Boolean available;
    private RatingResponse rating;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageResponse {
        private String url;
        private String alt;
        private Integer sortOrder;
        private Boolean isPrimary;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingResponse {
        private Double avg;
        private Integer count;
    }
}
