package com.ozdilek.ecommerce.controller;

import com.ozdilek.ecommerce.service.SeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/seed")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class SeedController {
    
    private final SeedService seedService;
    
    @PostMapping("/database")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> seedDatabase() {
        log.info("Database seeding endpoint called");
        
        try {
            seedService.seedDatabase();
            return ResponseEntity.ok(Map.of(
                "message", "Database seeded successfully",
                "status", "success"
            ));
        } catch (Exception e) {
            log.error("Error seeding database: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "message", "Error seeding database: " + e.getMessage(),
                "status", "error"
            ));
        }
    }
    
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getSeedStatus() {
        log.info("Seed status endpoint called");
        return ResponseEntity.ok(Map.of(
            "message", "Seed service is ready",
            "status", "ready"
        ));
    }
}
