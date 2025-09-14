package com.ozdilek.ecommerce.config;

import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        createAdminUser();
    }
    
    private void createAdminUser() {
        // Create Admin User
        String adminEmail = "admin@ozdilek.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name("Admin User")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin123!"))
                    .roles(List.of("ADMIN"))
                    .isVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
            log.info("Admin user created successfully: {}", adminEmail);
        }
        
        // Create Support User
        String supportEmail = "support@ozdilek.com";
        if (!userRepository.existsByEmail(supportEmail)) {
            User support = User.builder()
                    .name("Support User")
                    .email(supportEmail)
                    .passwordHash(passwordEncoder.encode("Support123!"))
                    .roles(List.of("SUPPORT"))
                    .isVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(support);
            log.info("Support user created successfully: {}", supportEmail);
        }
        
        // Create Seller User
        String sellerEmail = "seller@ozdilek.com";
        if (!userRepository.existsByEmail(sellerEmail)) {
            User seller = User.builder()
                    .name("Seller User")
                    .email(sellerEmail)
                    .passwordHash(passwordEncoder.encode("Seller123!"))
                    .roles(List.of("SELLER"))
                    .isVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(seller);
            log.info("Seller user created successfully: {}", sellerEmail);
        }
        
        // Create Test User
        String testEmail = "test@test.com";
        if (!userRepository.existsByEmail(testEmail)) {
            User test = User.builder()
                    .name("Test User")
                    .email(testEmail)
                    .passwordHash(passwordEncoder.encode("Test123!"))
                    .roles(List.of("USER"))
                    .isVerified(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(test);
            log.info("Test user created successfully: {}", testEmail);
        }
    }
}
