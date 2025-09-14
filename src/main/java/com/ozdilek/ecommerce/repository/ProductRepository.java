package com.ozdilek.ecommerce.repository;

import com.ozdilek.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    
    Optional<Product> findBySku(String sku);
    
    Optional<Product> findBySlug(String slug);
    
    boolean existsBySku(String sku);
    
    boolean existsBySlug(String slug);
    
    @Query("{ 'available': true, 'stock': { $gt: 0 } }")
    Page<Product> findAvailableProducts(Pageable pageable);
    
    @Query("{ 'categories': { $in: ?0 }, 'available': true, 'stock': { $gt: 0 } }")
    Page<Product> findByCategoriesAndAvailable(List<String> categoryIds, Pageable pageable);
    
    @Query("{ $text: { $search: ?0 }, 'available': true, 'stock': { $gt: 0 } }")
    Page<Product> findByTextSearchAndAvailable(String searchText, Pageable pageable);
    
    @Query("{ 'price': { $gte: ?0, $lte: ?1 }, 'available': true, 'stock': { $gt: 0 } }")
    Page<Product> findByPriceRangeAndAvailable(Double minPrice, Double maxPrice, Pageable pageable);
    
    @Query("{ 'categories': { $in: ?0 }, 'price': { $gte: ?1, $lte: ?2 }, 'available': true, 'stock': { $gt: 0 } }")
    Page<Product> findByCategoriesAndPriceRangeAndAvailable(List<String> categoryIds, Double minPrice, Double maxPrice, Pageable pageable);
    
    @Query("{ $text: { $search: ?0 }, 'categories': { $in: ?1 }, 'price': { $gte: ?2, $lte: ?3 }, 'available': true, 'stock': { $gt: 0 } }")
    Page<Product> findByTextSearchAndCategoriesAndPriceRangeAndAvailable(String searchText, List<String> categoryIds, Double minPrice, Double maxPrice, Pageable pageable);
}
