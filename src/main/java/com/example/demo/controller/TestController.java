package com.example.demo.controller;

import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    public TestController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }
    
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "서버가 정상적으로 실행 중입니다.");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/hello")
    public String hello() {
        return "안녕하세요! 알림 시스템 테스트 서버입니다.";
    }
    
    @PostMapping("/echo")
    public Map<String, Object> echo(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "요청을 받았습니다.");
        response.put("data", request);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "알림 시스템");
        info.put("version", "1.0.0");
        info.put("java.version", System.getProperty("java.version"));
        info.put("os.name", System.getProperty("os.name"));
        info.put("user.timezone", System.getProperty("user.timezone"));
        return info;
    }
    
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 사용자 통계
        stats.put("totalUsers", userService.getAllUsers().size());
        stats.put("activeUsers", userService.getActiveUsers().size());
        
        // 알림 통계
        stats.put("totalNotifications", notificationService.getAllNotifications().size());
        stats.put("pendingNotifications", notificationService.getNotificationsByStatus("PENDING").size());
        stats.put("sentNotifications", notificationService.getNotificationsByStatus("SENT").size());
        stats.put("failedNotifications", notificationService.getNotificationsByStatus("FAILED").size());
        
        return stats;
    }
} 