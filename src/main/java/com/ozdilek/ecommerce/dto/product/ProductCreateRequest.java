package com.ozdilek.ecommerce.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ProductCreateRequest {
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Slug is required")
    private String slug;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @Builder.Default
    private String currency = "TRY";
    
    @NotNull(message = "Categories are required")
    @NotEmpty(message = "At least one category must be specified")
    private List<String> categories;
    
    private List<ProductImageRequest> images;
    
    private Map<String, Object> attributes;
    
    @Builder.Default
    private Integer stock = 0;
    
    @Builder.Default
    private Boolean available = true;
    
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
