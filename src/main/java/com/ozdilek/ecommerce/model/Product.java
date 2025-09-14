package com.ozdilek.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    
    @Id
    private String id;
    
    @NotBlank
    @Indexed(unique = true)
    private String sku;
    
    @NotBlank
    @TextIndexed(weight = 10)
    private String title;
    
    @NotBlank
    @Indexed(unique = true)
    private String slug;
    
    @TextIndexed(weight = 5)
    private String description;
    
    @NotNull
    @Positive
    private BigDecimal price;
    
    @Builder.Default
    private String currency = "TRY";
    
    @NotNull
    private List<String> categories;
    
    private List<ProductImage> images;
    
    private Map<String, Object> attributes;
    
    @Builder.Default
    private Integer stock = 0;
    
    @Builder.Default
    private Boolean available = true;
    
    private Rating rating;
    
    private List<String> tags;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImage {
        private String url;
        private String alt;
        private Integer sortOrder;
        private Boolean isPrimary;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rating {
        @Builder.Default
        private Double avg = 0.0;
        
        @Builder.Default
        private Integer count = 0;
    }
}
