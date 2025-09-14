package com.ozdilek.ecommerce.service;

import com.ozdilek.ecommerce.dto.auth.AuthResponse;
import com.ozdilek.ecommerce.dto.auth.LoginRequest;
import com.ozdilek.ecommerce.dto.auth.RegisterRequest;
import com.ozdilek.ecommerce.model.RefreshToken;
import com.ozdilek.ecommerce.model.User;
import com.ozdilek.ecommerce.repository.RefreshTokenRepository;
import com.ozdilek.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitingService rateLimitingService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists with email: " + request.getEmail());
        }
        
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .roles(Collections.singletonList("USER"))
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());
        
        return generateAuthResponse(user);
    }
    
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        // Check if login is blocked due to too many failed attempts
        if (rateLimitingService.isLoginBlocked(request.getEmail())) {
            long blockDuration = rateLimitingService.getLoginBlockDuration(request.getEmail());
            throw new RuntimeException("Login temporarily blocked. Try again in " + (blockDuration / 60) + " minutes.");
        }
        
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            
            // Clear failed attempts on successful login
            rateLimitingService.clearFailedLoginAttempts(request.getEmail());
            
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("User logged in successfully with ID: {}", user.getId());
            return generateAuthResponse(user);
            
        } catch (BadCredentialsException e) {
            // Record failed attempt
            rateLimitingService.recordFailedLoginAttempt(request.getEmail());
            int remainingAttempts = rateLimitingService.getRemainingLoginAttempts(request.getEmail());
            
            log.warn("Failed login attempt for email: {}. Remaining attempts: {}", request.getEmail(), remainingAttempts);
            throw new RuntimeException("Invalid credentials. Remaining attempts: " + remainingAttempts);
        }
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return generateAuthResponse(user);
    }
    
    private AuthResponse generateAuthResponse(User user) {
        UserDetails userDetails = createUserDetails(user);
        
        String accessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);
        
        // Save refresh token
        refreshTokenService.saveRefreshToken(user.getId(), newRefreshToken);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .roles(user.getRoles())
                        .isVerified(user.getIsVerified())
                        .build())
                .build();
    }
    
    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(user.getRoles().toArray(new String[0]))
                .build();
    }
}
