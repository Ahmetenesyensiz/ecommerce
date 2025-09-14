package com.ozdilek.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class Category {
    
    @Id
    private String id;
    
    @NotBlank
    private String name;
    
    @NotBlank
    @Indexed(unique = true)
    private String slug;
    
    private String parentId;
    
    @Builder.Default
    private Integer sortOrder = 0;
    
    private String description;
    
    private String imageUrl;
    
    @Builder.Default
    private Boolean active = true;
}
