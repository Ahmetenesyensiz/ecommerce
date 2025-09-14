package com.ozdilek.ecommerce.controller;

import com.ozdilek.ecommerce.model.Category;
import com.ozdilek.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        log.info("Get all categories endpoint called");
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable String id) {
        log.info("Get category by id endpoint called with id: {}", id);
        Category category = categoryService.findById(id);
        return ResponseEntity.ok(category);
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Category> getCategoryBySlug(@PathVariable String slug) {
        log.info("Get category by slug endpoint called with slug: {}", slug);
        Category category = categoryService.findBySlug(slug);
        return ResponseEntity.ok(category);
    }
    
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<Category>> getCategoriesByParentId(@PathVariable(required = false) String parentId) {
        log.info("Get categories by parent id endpoint called with parentId: {}", parentId);
        List<Category> categories = categoryService.findByParentId(parentId);
        return ResponseEntity.ok(categories);
    }
}
