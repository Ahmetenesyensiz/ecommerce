package com.ozdilek.ecommerce.repository;

import com.ozdilek.ecommerce.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    
    Optional<Category> findBySlug(String slug);
    
    List<Category> findByParentIdIsNullOrderBySortOrderAsc();
    
    List<Category> findByParentIdOrderBySortOrderAsc(String parentId);
    
    List<Category> findByActiveTrueOrderBySortOrderAsc();
    
    boolean existsBySlug(String slug);
}
