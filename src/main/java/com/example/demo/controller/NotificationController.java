package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping("/send/user/{userId}")
    public ResponseEntity<String> sendNotificationToUser(
            @PathVariable String userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String type) {
        
        notificationService.sendNotificationToUser(userId, title, message, type);
        return ResponseEntity.ok("알림이 큐에 추가되었습니다.");
    }
    
    @PostMapping("/send/all")
    public ResponseEntity<String> sendNotificationToAllUsers(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String type) {
        
        notificationService.sendNotificationToAllUsers(title, message, type);
        return ResponseEntity.ok("모든 사용자에게 알림이 큐에 추가되었습니다.");
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> getNotificationsByStatus(@PathVariable String status) {
        List<Notification> notifications = notificationService.getNotificationsByStatus(status);
        return ResponseEntity.ok(notifications);
    }
} 