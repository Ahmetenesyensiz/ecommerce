package com.ozdilek.ecommerce.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    
    private String q; // search query
    
    private List<String> categories;
    
    private Double minPrice;
    
    private Double maxPrice;
    
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
    
    private String sort; // price-asc, price-desc, name-asc, name-desc, newest
    
    private Boolean available;
}
