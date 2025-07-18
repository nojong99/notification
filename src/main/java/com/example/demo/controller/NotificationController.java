package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    @PostMapping("/send/user/{userId}")
    public ResponseEntity<String> sendNotificationToUser(
            @PathVariable String userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String type) {
        
        // 관리자 권한 확인
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
        }
        
        notificationService.sendNotificationToUser(userId, title, message, type);
        return ResponseEntity.ok("알림이 큐에 추가되었습니다.");
    }
    
    @PostMapping("/send/all")
    public ResponseEntity<String> sendNotificationToAllUsers(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String type) {
        
        // 관리자 권한 확인
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
        }
        
        notificationService.sendNotificationToAllUsers(title, message, type);
        return ResponseEntity.ok("모든 사용자에게 알림이 큐에 추가되었습니다.");
    }
    
    @PostMapping("/process/{notificationId}")
    public ResponseEntity<String> processNotification(@PathVariable String notificationId) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
        }
        
        notificationService.processNotification(notificationId);
        return ResponseEntity.ok("알림이 처리되었습니다.");
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> getNotificationsByStatus(@PathVariable String status) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        List<Notification> notifications = notificationService.getNotificationsByStatus(status);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<Notification>> getPendingNotifications() {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        List<Notification> notifications = notificationService.getNotificationsByStatus("PENDING");
        return ResponseEntity.ok(notifications);
    }
    
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return userService.isAdmin(username);
        }
        return false;
    }
} 