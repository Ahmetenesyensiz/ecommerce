package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.model.Category;
import com.ozdilek.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Transactional(readOnly = true)
    // @Cacheable(value = "categories") // Disabled for testing
    public List<Category> getAllCategories() {
        log.info("Getting all categories");
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc();
    }
    
    @Transactional(readOnly = true)
    // @Cacheable(value = "categories", key = "#id") // Disabled for testing
    public Category findById(String id) {
        log.info("Finding category by id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    // @Cacheable(value = "categories", key = "#slug") // Disabled for testing
    public Category findBySlug(String slug) {
        log.info("Finding category by slug: {}", slug);
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Category not found with slug: " + slug));
    }
    
    @Transactional(readOnly = true)
    // @Cacheable(value = "categories", key = "'parent-' + #parentId") // Disabled for testing
    public List<Category> findByParentId(String parentId) {
        log.info("Finding categories by parent id: {}", parentId);
        if (parentId == null) {
            return categoryRepository.findByParentIdIsNullOrderBySortOrderAsc();
        }
        return categoryRepository.findByParentIdOrderBySortOrderAsc(parentId);
    }
}
