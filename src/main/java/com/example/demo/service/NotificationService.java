package com.example.demo.service;

import com.example.demo.dto.NotificationMessage;
import com.example.demo.kafka.NotificationProducer;
import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationProducer notificationProducer;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    
    public void sendNotificationToUser(String userId, String title, String message, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        // 알림 메시지 생성
        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setUserId(userId);
        notificationMessage.setTitle(title);
        notificationMessage.setMessage(message);
        notificationMessage.setType(type);
        notificationMessage.setEmail(user.getEmail());
        notificationMessage.setPhoneNumber(user.getPhoneNumber());
        
        // Kafka로 메시지 전송
        notificationProducer.sendNotification(notificationMessage);
        
        // 알림 기록 저장
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setStatus("PENDING");
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        log.info("알림 요청이 큐에 추가되었습니다: {}", title);
    }
    
    public void sendNotificationToAllUsers(String title, String message, String type) {
        List<User> activeUsers = userRepository.findByActiveTrue();
        
        for (User user : activeUsers) {
            sendNotificationToUser(user.getId(), title, message, type);
        }
        
        log.info("모든 활성 사용자에게 알림 요청이 추가되었습니다. 총 {}명", activeUsers.size());
    }
    
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserId(userId);
    }
    
    public List<Notification> getNotificationsByStatus(String status) {
        return notificationRepository.findByStatus(status);
    }
} 