package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Notification;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {
    
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    
    @GetMapping("/mongodb-status")
    public Map<String, Object> getMongoDBStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 사용자 수 확인
            long userCount = userRepository.count();
            List<User> users = userRepository.findAll();
            
            // 알림 수 확인
            long notificationCount = notificationRepository.count();
            List<Notification> notifications = notificationRepository.findAll();
            
            result.put("status", "success");
            result.put("userCount", userCount);
            result.put("notificationCount", notificationCount);
            result.put("users", users);
            result.put("notifications", notifications);
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("exception", e.getClass().getSimpleName());
        }
        
        return result;
    }
    
    @GetMapping("/test-user")
    public Map<String, Object> createTestUser() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 테스트 사용자 생성
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            testUser.setPassword("password");
            testUser.setRole(User.UserRole.USER);
            testUser.setActive(true);
            
            User savedUser = userRepository.save(testUser);
            
            result.put("status", "success");
            result.put("message", "테스트 사용자가 생성되었습니다.");
            result.put("user", savedUser);
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        
        return result;
    }
} 