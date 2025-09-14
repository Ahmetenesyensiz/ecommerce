package com.ozdilek.ecommerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminActivityLogService {
    
    // In-memory storage for demo purposes. In production, use database
    private final Map<String, List<AdminActivity>> activityLogs = new ConcurrentHashMap<>();
    
    public void logAdminActivity(String adminId, String action, String resource, String resourceId, Map<String, Object> details) {
        log.info("Admin activity: {} performed {} on {} with id {}", adminId, action, resource, resourceId);
        
        AdminActivity activity = AdminActivity.builder()
                .adminId(adminId)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        
        activityLogs.computeIfAbsent(adminId, k -> new ArrayList<>()).add(activity);
        
        // Keep only last 1000 activities per admin
        List<AdminActivity> adminActivities = activityLogs.get(adminId);
        if (adminActivities.size() > 1000) {
            adminActivities.subList(0, adminActivities.size() - 1000).clear();
        }
    }
    
    public void logUserManagement(String adminId, String action, String userId, Map<String, Object> details) {
        logAdminActivity(adminId, action, "USER", userId, details);
    }
    
    public void logProductManagement(String adminId, String action, String productId, Map<String, Object> details) {
        logAdminActivity(adminId, action, "PRODUCT", productId, details);
    }
    
    public void logOrderManagement(String adminId, String action, String orderId, Map<String, Object> details) {
        logAdminActivity(adminId, action, "ORDER", orderId, details);
    }
    
    public void logSecurityEvent(String adminId, String action, Map<String, Object> details) {
        logAdminActivity(adminId, action, "SECURITY", null, details);
    }
    
    public List<AdminActivity> getAdminActivities(String adminId, int limit) {
        List<AdminActivity> activities = activityLogs.getOrDefault(adminId, new ArrayList<>());
        return activities.stream()
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .limit(limit)
                .toList();
    }
    
    public List<AdminActivity> getAllActivities(int limit) {
        return activityLogs.values().stream()
                .flatMap(List::stream)
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .limit(limit)
                .toList();
    }
    
    public List<AdminActivity> getActivitiesByResource(String resource, int limit) {
        return activityLogs.values().stream()
                .flatMap(List::stream)
                .filter(activity -> resource.equals(activity.getResource()))
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .limit(limit)
                .toList();
    }
    
    public Map<String, Object> getActivityStatistics() {
        long totalActivities = activityLogs.values().stream()
                .mapToLong(List::size)
                .sum();
        
        long uniqueAdmins = activityLogs.size();
        
        Map<String, Long> actionCounts = new ConcurrentHashMap<>();
        Map<String, Long> resourceCounts = new ConcurrentHashMap<>();
        
        activityLogs.values().stream()
                .flatMap(List::stream)
                .forEach(activity -> {
                    actionCounts.merge(activity.getAction(), 1L, Long::sum);
                    resourceCounts.merge(activity.getResource(), 1L, Long::sum);
                });
        
        return Map.of(
            "totalActivities", totalActivities,
            "uniqueAdmins", uniqueAdmins,
            "actionCounts", actionCounts,
            "resourceCounts", resourceCounts
        );
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AdminActivity {
        private String adminId;
        private String action;
        private String resource;
        private String resourceId;
        private Map<String, Object> details;
        private LocalDateTime timestamp;
    }
}
