package com.ozdilek.ecommerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitingService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String LOGIN_ATTEMPTS_KEY = "login_attempts:";
    private static final String IP_RATE_LIMIT_KEY = "rate_limit:";
    
    // Login attempt limits
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_ATTEMPTS_WINDOW = Duration.ofMinutes(15);
    
    // General rate limits
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);
    
    public boolean isLoginBlocked(String email) {
        String key = LOGIN_ATTEMPTS_KEY + email;
        String attempts = redisTemplate.opsForValue().get(key);
        
        if (attempts == null) {
            return false;
        }
        
        int attemptCount = Integer.parseInt(attempts);
        return attemptCount >= MAX_LOGIN_ATTEMPTS;
    }
    
    public void recordFailedLoginAttempt(String email) {
        String key = LOGIN_ATTEMPTS_KEY + email;
        
        String attempts = redisTemplate.opsForValue().get(key);
        int attemptCount = attempts == null ? 0 : Integer.parseInt(attempts);
        
        attemptCount++;
        redisTemplate.opsForValue().set(key, String.valueOf(attemptCount), LOGIN_ATTEMPTS_WINDOW);
        
        log.warn("Failed login attempt {} for email: {}", attemptCount, email);
    }
    
    public void clearFailedLoginAttempts(String email) {
        String key = LOGIN_ATTEMPTS_KEY + email;
        redisTemplate.delete(key);
        log.info("Cleared failed login attempts for email: {}", email);
    }
    
    public boolean isRateLimited(String ipAddress) {
        String key = IP_RATE_LIMIT_KEY + ipAddress;
        String requests = redisTemplate.opsForValue().get(key);
        
        if (requests == null) {
            return false;
        }
        
        int requestCount = Integer.parseInt(requests);
        return requestCount >= MAX_REQUESTS_PER_MINUTE;
    }
    
    public void recordRequest(String ipAddress) {
        String key = IP_RATE_LIMIT_KEY + ipAddress;
        
        String requests = redisTemplate.opsForValue().get(key);
        int requestCount = requests == null ? 0 : Integer.parseInt(requests);
        
        requestCount++;
        redisTemplate.opsForValue().set(key, String.valueOf(requestCount), RATE_LIMIT_WINDOW);
    }
    
    public int getRemainingLoginAttempts(String email) {
        String key = LOGIN_ATTEMPTS_KEY + email;
        String attempts = redisTemplate.opsForValue().get(key);
        
        if (attempts == null) {
            return MAX_LOGIN_ATTEMPTS;
        }
        
        int attemptCount = Integer.parseInt(attempts);
        return Math.max(0, MAX_LOGIN_ATTEMPTS - attemptCount);
    }
    
    public long getLoginBlockDuration(String email) {
        String key = LOGIN_ATTEMPTS_KEY + email;
        Long ttl = redisTemplate.getExpire(key);
        
        if (ttl == null || ttl == -1) {
            return 0;
        }
        
        return ttl;
    }
}
