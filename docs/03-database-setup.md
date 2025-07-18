# 🗄️ 데이터베이스 설정 및 모델 생성

## 📝 1단계: User 모델 생성

사용자 정보를 저장할 `User` 클래스를 만들어요.

`src/main/java/com/example/demo/model/User.java` 파일을 생성:

```java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true)
    private String email;
    
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;
    
    private boolean active = true;
    
    // 사용자 역할 열거형
    public enum UserRole {
        ADMIN, USER
    }
    
    // 생성자 (ID 제외)
    public User(String username, String password, String email, String phoneNumber, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.active = true;
    }
}
```

## 📝 2단계: Notification 모델 생성

알림 정보를 저장할 `Notification` 클래스를 만들어요.

`src/main/java/com/example/demo/model/Notification.java` 파일을 생성:

```java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;  // 알림을 받을 사용자 ID
    
    @Column(nullable = false)
    private String title;   // 알림 제목
    
    @Column(nullable = false)
    private String message; // 알림 내용
    
    @Enumerated(EnumType.STRING)
    private NotificationType type = NotificationType.INFO;
    
    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime sentAt;
    
    // 알림 타입 열거형
    public enum NotificationType {
        INFO, WARNING, ERROR
    }
    
    // 알림 상태 열거형
    public enum NotificationStatus {
        PENDING, SENT, FAILED, READ
    }
    
    // 생성자 (ID, 시간 제외)
    public Notification(String userId, String title, String message, NotificationType type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.status = NotificationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}
```

## 📝 3단계: Repository 인터페이스 생성

데이터베이스에 접근할 Repository 클래스들을 만들어요.

### UserRepository 생성

`src/main/java/com/example/demo/repository/UserRepository.java` 파일을 생성:

```java
package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 사용자명으로 사용자 찾기
    Optional<User> findByUsername(String username);
    
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // 활성 사용자만 찾기
    List<User> findByActiveTrue();
    
    // 역할로 사용자 찾기
    List<User> findByRole(User.UserRole role);
    
    // 사용자명이 존재하는지 확인
    boolean existsByUsername(String username);
    
    // 이메일이 존재하는지 확인
    boolean existsByEmail(String email);
}
```

### NotificationRepository 생성

`src/main/java/com/example/demo/repository/NotificationRepository.java` 파일을 생성:

```java
package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // 특정 사용자의 알림 찾기
    List<Notification> findByUserId(String userId);
    
    // 상태별 알림 찾기
    List<Notification> findByStatus(Notification.NotificationStatus status);
    
    // 사용자와 상태로 알림 찾기
    List<Notification> findByUserIdAndStatus(String userId, Notification.NotificationStatus status);
    
    // 생성일 기준 내림차순 정렬
    List<Notification> findAllByOrderByCreatedAtDesc();
    
    // 상태별 알림 개수 세기
    long countByStatus(Notification.NotificationStatus status);
}
```

## 📝 4단계: DTO 클래스 생성

데이터 전송 객체(DTO)를 만들어요.

### UserRegistrationDto 생성

`src/main/java/com/example/demo/dto/UserRegistrationDto.java` 파일을 생성:

```java
package com.example.demo.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
}
```

## 📝 5단계: 데이터베이스 초기화 서비스 생성

애플리케이션 시작 시 테스트 데이터를 생성하는 서비스를 만들어요.

`src/main/java/com/example/demo/service/DataInitializationService.java` 파일을 생성:

```java
package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 기존 데이터가 없을 때만 초기 데이터 생성
        if (userRepository.count() == 0) {
            createInitialUsers();
            log.info("초기 사용자 데이터가 생성되었습니다.");
        }
    }
    
    private void createInitialUsers() {
        // 관리자 계정 생성
        User admin = new User(
            "admin",
            passwordEncoder.encode("admin123"),
            "admin@example.com",
            "010-1234-5678",
            User.UserRole.ADMIN
        );
        userRepository.save(admin);
        
        // 일반 사용자 계정들 생성
        User user1 = new User(
            "user1",
            passwordEncoder.encode("password1"),
            "user1@example.com",
            "010-1111-1111",
            User.UserRole.USER
        );
        userRepository.save(user1);
        
        User user2 = new User(
            "user2",
            passwordEncoder.encode("password2"),
            "user2@example.com",
            "010-2222-2222",
            User.UserRole.USER
        );
        userRepository.save(user2);
        
        User user3 = new User(
            "user3",
            passwordEncoder.encode("password3"),
            "user3@example.com",
            "010-3333-3333",
            User.UserRole.USER
        );
        userRepository.save(user3);
    }
}
```

## 📝 6단계: 프로젝트 빌드 및 테스트

이제 데이터베이스 모델이 제대로 설정되었는지 테스트해요.

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

## 📝 7단계: H2 데이터베이스 콘솔 확인

애플리케이션이 실행되면 H2 데이터베이스 콘솔에 접속해서 테이블이 생성되었는지 확인해요.

1. **브라우저에서 접속**: `http://localhost:8081/h2-console`
2. **JDBC URL**: `jdbc:h2:mem:testdb`
3. **사용자명**: `sa`
4. **비밀번호**: (비어있음)
5. **Connect** 버튼 클릭

### 확인할 테이블들:
- `USERS` 테이블: 사용자 정보
- `NOTIFICATIONS` 테이블: 알림 정보

### SQL 쿼리로 데이터 확인:
```sql
-- 사용자 목록 확인
SELECT * FROM USERS;

-- 알림 목록 확인
SELECT * FROM NOTIFICATIONS;
```

## ✅ 확인사항

데이터베이스 설정이 성공적으로 완료되면:

1. **빌드 성공**: `BUILD SUCCESSFUL` 메시지
2. **테이블 생성**: H2 콘솔에서 `USERS`, `NOTIFICATIONS` 테이블 확인
3. **초기 데이터**: 관리자와 테스트 사용자 계정들이 생성됨

## 🚨 문제 해결

### 오류 1: JPA 관련 오류
```
No identifier specified for entity
```
**해결**: `@Id` 어노테이션이 제대로 설정되었는지 확인하세요.

### 오류 2: 데이터베이스 연결 실패
```
Database not found
```
**해결**: `application.properties`의 데이터베이스 설정을 확인하세요.

### 오류 3: 테이블 생성 실패
```
Table already exists
```
**해결**: 애플리케이션을 재시작하세요. H2는 메모리 데이터베이스라서 재시작하면 초기화됩니다.

## 🚀 다음 단계

데이터베이스 설정이 완료되면 다음 문서로 이동하세요:
👉 **[Spring Security 인증 시스템 구현](./04-security-setup.md)**

---

## 💡 팁

- **JPA**: Java에서 데이터베이스를 쉽게 사용할 수 있게 도와주는 기술이에요
- **Entity**: 데이터베이스 테이블과 연결되는 Java 클래스예요
- **Repository**: 데이터베이스에 데이터를 저장하고 가져오는 인터페이스예요
- **DTO**: 데이터를 전송할 때 사용하는 객체예요
- 모든 클래스는 **package** 선언을 꼭 포함해야 해요 