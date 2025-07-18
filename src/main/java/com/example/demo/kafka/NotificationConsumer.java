package com.example.demo.kafka;

import com.example.demo.dto.NotificationMessage;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    
    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    
    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void consumeNotification(String message) {
        try {
            NotificationMessage notificationMessage = objectMapper.readValue(message, NotificationMessage.class);
            log.info("알림 메시지 수신: {}", notificationMessage);
            
            // 알림 처리 로직
            processNotification(notificationMessage);
            
        } catch (Exception e) {
            log.error("알림 메시지 처리 오류: {}", e.getMessage());
        }
    }
    
    private void processNotification(NotificationMessage message) {
        try {
            // 실제 알림 전송 로직 (이메일, SMS, 푸시 등)
            log.info("사용자 {}에게 {} 알림 전송: {}", 
                    message.getUserId(), 
                    message.getType(), 
                    message.getTitle());
            
            // 알림 상태 업데이트
            Notification notification = new Notification();
            notification.setUserId(message.getUserId());
            notification.setTitle(message.getTitle());
            notification.setMessage(message.getMessage());
            notification.setType(message.getType());
            notification.setStatus("SENT");
            notification.setCreatedAt(LocalDateTime.now());
            notification.setSentAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            
            // 실제 알림 전송 시뮬레이션
            Thread.sleep(1000); // 1초 대기로 실제 전송 시뮬레이션
            
            log.info("알림 전송 완료: {}", message.getTitle());
            
        } catch (Exception e) {
            log.error("알림 처리 실패: {}", e.getMessage());
            
            // 실패한 알림 저장
            Notification failedNotification = new Notification();
            failedNotification.setUserId(message.getUserId());
            failedNotification.setTitle(message.getTitle());
            failedNotification.setMessage(message.getMessage());
            failedNotification.setType(message.getType());
            failedNotification.setStatus("FAILED");
            failedNotification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(failedNotification);
        }
    }
} 