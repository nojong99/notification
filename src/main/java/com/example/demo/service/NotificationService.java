package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class NotificationService {
    
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    
    @Autowired
    public NotificationService(UserRepository userRepository, 
                             NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }
    
    public void sendNotificationToUser(String userId, String title, String message, String type) {
        // userId가 문자열이므로 username으로 사용자 찾기
        User user = userRepository.findByUsername(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + userId);
        }
        
        // 알림 기록 먼저 저장
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setStatus("PENDING");
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
        
        // 로컬에서 직접 처리
        processNotificationLocally(notification);
    }
    
    public void sendNotificationToAllUsers(String title, String message, String type) {
        List<User> activeUsers = userRepository.findByActiveTrue();
        
        for (User user : activeUsers) {
            sendNotificationToUser(user.getUsername(), title, message, type);
        }
        
        log.info("모든 활성 사용자에게 알림 요청이 추가되었습니다. 총 {}명", activeUsers.size());
    }
    
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserId(userId);
    }
    
    public List<Notification> getNotificationsByStatus(String status) {
        return notificationRepository.findByStatus(status);
    }
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<User> getAllUsers() {
        return userRepository.findByActiveTrue();
    }
    
    public void resendNotification(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다: " + id));
        
        // 상태를 PENDING으로 변경하여 재전송
        notification.setStatus("PENDING");
        notification.setSentAt(null);
        notificationRepository.save(notification);
        
        log.info("알림 재전송 요청: {}", notification.getTitle());
    }
    
    public void deleteNotification(String id) {
        notificationRepository.deleteById(id);
        log.info("알림 삭제됨: ID {}", id);
    }
    
    public void markNotificationAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다: " + id));
        notification.setStatus("READ");
        notificationRepository.save(notification);
        log.info("알림 읽음 처리: ID {}", id);
    }
    
    public void markAllNotificationsAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndStatus(userId, "PENDING");
        for (Notification notification : notifications) {
            notification.setStatus("READ");
        }
        notificationRepository.saveAll(notifications);
        log.info("모든 알림 읽음 처리: 사용자 {}", userId);
    }
    
    // 로컬에서 알림 처리
    private void processNotificationLocally(Notification notification) {
        try {
            log.info("로컬에서 알림 처리: {}에게 {} 알림 - {}", 
                    notification.getUserId(), notification.getType(), notification.getTitle());
            
            // 실제 알림 전송 시뮬레이션
            Thread.sleep(500); // 0.5초 대기
            
            // 알림 상태 업데이트
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            log.info("로컬 알림 처리 완료: {}", notification.getTitle());
            
        } catch (Exception e) {
            log.error("로컬 알림 처리 실패: {}", e.getMessage());
            
            // 실패 상태 업데이트
            notification.setStatus("FAILED");
            notificationRepository.save(notification);
        }
    }
    
    // 관리자가 알림을 수동으로 처리하는 메서드
    public void processNotification(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다: " + notificationId));
        
        if ("PENDING".equals(notification.getStatus())) {
            // 알림 처리 시뮬레이션
            try {
                log.info("관리자가 알림을 처리합니다: {}", notification.getTitle());
                Thread.sleep(1000); // 1초 대기
                
                notification.setStatus("SENT");
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
                
                log.info("알림 처리 완료: {}", notification.getTitle());
            } catch (Exception e) {
                log.error("알림 처리 실패: {}", e.getMessage());
                notification.setStatus("FAILED");
                notificationRepository.save(notification);
            }
        } else {
            throw new RuntimeException("이미 처리된 알림입니다: " + notification.getStatus());
        }
    }
    
    // 알림 통계 조회
    public NotificationStats getNotificationStats() {
        long total = notificationRepository.count();
        long pending = notificationRepository.countByStatus("PENDING");
        long sent = notificationRepository.countByStatus("SENT");
        long failed = notificationRepository.countByStatus("FAILED");
        long read = notificationRepository.countByStatus("READ");
        
        return new NotificationStats(total, pending, sent, failed, read);
    }
    
    // 알림 통계 클래스
    public static class NotificationStats {
        private final long total;
        private final long pending;
        private final long sent;
        private final long failed;
        private final long read;
        
        public NotificationStats(long total, long pending, long sent, long failed, long read) {
            this.total = total;
            this.pending = pending;
            this.sent = sent;
            this.failed = failed;
            this.read = read;
        }
        
        // Getters
        public long getTotal() { return total; }
        public long getPending() { return pending; }
        public long getSent() { return sent; }
        public long getFailed() { return failed; }
        public long getRead() { return read; }
    }
} 