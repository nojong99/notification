# 🔔 알림 시스템 핵심 기능 구현

## 📝 1단계: NotificationService 생성

알림 관련 비즈니스 로직을 처리하는 서비스를 만들어요.

`src/main/java/com/example/demo/service/NotificationService.java` 파일을 생성:

```java
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
        notification.setType(Notification.NotificationType.valueOf(type));
        notification.setStatus(Notification.NotificationStatus.PENDING);
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
        return notificationRepository.findByStatus(Notification.NotificationStatus.valueOf(status));
    }
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<User> getAllUsers() {
        return userRepository.findByActiveTrue();
    }
    
    public void resendNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다: " + id));
        
        // 상태를 PENDING으로 변경하여 재전송
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setSentAt(null);
        notificationRepository.save(notification);
        
        log.info("알림 재전송 요청: {}", notification.getTitle());
    }
    
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
        log.info("알림 삭제됨: ID {}", id);
    }
    
    public void markNotificationAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다: " + id));
        notification.setStatus(Notification.NotificationStatus.READ);
        notificationRepository.save(notification);
        log.info("알림 읽음 처리: ID {}", id);
    }
    
    public void markAllNotificationsAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndStatus(userId, Notification.NotificationStatus.PENDING);
        for (Notification notification : notifications) {
            notification.setStatus(Notification.NotificationStatus.READ);
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
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            log.info("로컬 알림 처리 완료: {}", notification.getTitle());
            
        } catch (Exception e) {
            log.error("로컬 알림 처리 실패: {}", e.getMessage());
            
            // 실패 상태 업데이트
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }
    }
    
    // 관리자가 알림을 수동으로 처리하는 메서드
    public void processNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다: " + notificationId));
        
        if (Notification.NotificationStatus.PENDING.equals(notification.getStatus())) {
            // 알림 처리 시뮬레이션
            try {
                log.info("관리자가 알림을 처리합니다: {}", notification.getTitle());
                Thread.sleep(1000); // 1초 대기
                
                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
                
                log.info("알림 처리 완료: {}", notification.getTitle());
            } catch (Exception e) {
                log.error("알림 처리 실패: {}", e.getMessage());
                notification.setStatus(Notification.NotificationStatus.FAILED);
                notificationRepository.save(notification);
            }
        } else {
            throw new RuntimeException("이미 처리된 알림입니다: " + notification.getStatus());
        }
    }
    
    // 알림 통계 조회
    public NotificationStats getNotificationStats() {
        long total = notificationRepository.count();
        long pending = notificationRepository.countByStatus(Notification.NotificationStatus.PENDING);
        long sent = notificationRepository.countByStatus(Notification.NotificationStatus.SENT);
        long failed = notificationRepository.countByStatus(Notification.NotificationStatus.FAILED);
        long read = notificationRepository.countByStatus(Notification.NotificationStatus.READ);
        
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
```

## 📝 2단계: NotificationController 생성

알림 관련 API를 처리하는 컨트롤러를 만들어요.

`src/main/java/com/example/demo/controller/NotificationController.java` 파일을 생성:

```java
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
    public ResponseEntity<String> processNotification(@PathVariable Long notificationId) {
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
```

## 📝 3단계: TestController 생성

API 테스트를 위한 컨트롤러를 만들어요.

`src/main/java/com/example/demo/controller/TestController.java` 파일을 생성:

```java
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
```

## 📝 4단계: 프로젝트 빌드 및 테스트

이제 알림 시스템이 제대로 설정되었는지 테스트해요.

### Windows에서:
```cmd
# 프로젝트 빌드
gradlew build

# 애플리케이션 실행
gradlew bootRun
```

### Mac/Linux에서:
```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

## 📝 5단계: API 테스트

애플리케이션이 실행되면 API를 테스트해요.

### 1. 서버 상태 확인
```bash
curl http://localhost:8081/api/test/health
```

### 2. 알림 전송 테스트 (관리자 계정으로 로그인 후)
```bash
curl -X POST "http://localhost:8081/api/notifications/send/user/user1?title=테스트&message=안녕하세요&type=INFO"
```

### 3. 통계 확인
```bash
curl http://localhost:8081/api/test/stats
```

## ✅ 확인사항

알림 시스템이 성공적으로 설정되면:

1. **빌드 성공**: `BUILD SUCCESSFUL` 메시지
2. **API 접근**: `/api/test/health` 등 API 엔드포인트 접근 가능
3. **알림 전송**: 관리자 권한으로 알림 전송 가능
4. **데이터베이스**: H2 콘솔에서 알림 데이터 확인 가능

## 🚀 다음 단계

알림 시스템이 완료되면 다음 문서로 이동하세요:
👉 **[웹 인터페이스 구현](./06-web-interface.md)**

---

## 💡 팁

- **Service**: 비즈니스 로직을 처리하는 클래스예요
- **Controller**: 웹 요청을 처리하는 클래스예요
- **API**: 다른 프로그램과 데이터를 주고받는 인터페이스예요
- **REST**: 웹 API를 설계하는 방식 중 하나예요 