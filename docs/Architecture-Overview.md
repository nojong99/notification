# 아키텍처 개요 (Architecture Overview)

사용자 알림 시스템의 전체 아키텍처와 설계 원칙에 대한 개요입니다.

## 📋 목차

- [시스템 개요](#시스템-개요)
- [아키텍처 다이어그램](#아키텍처-다이어그램)
- [컴포넌트 설명](#컴포넌트-설명)
- [데이터 플로우](#데이터-플로우)
- [기술 스택](#기술-스택)
- [확장성 고려사항](#확장성-고려사항)

## 🏗️ 시스템 개요

### 목적
사용자에게 다양한 채널(이메일, SMS, 푸시)을 통해 알림을 전송하는 확장 가능한 시스템을 제공합니다.

### 핵심 기능
- **사용자 관리**: 사용자 정보 CRUD 및 활성 상태 관리
- **알림 전송**: 다중 채널을 통한 알림 전송
- **비동기 처리**: Kafka를 통한 고성능 메시지 처리
- **상태 추적**: 알림 전송 상태 실시간 모니터링
- **대량 전송**: 모든 활성 사용자에게 일괄 알림 전송

### 설계 원칙
- **느슨한 결합**: 컴포넌트 간 의존성 최소화
- **확장성**: 수평 확장 가능한 아키텍처
- **내구성**: 메시지 손실 방지 및 장애 복구
- **성능**: 고처리량 및 낮은 지연시간
- **모니터링**: 실시간 상태 추적 및 로깅

## 📊 아키텍처 다이어그램

### 전체 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │   Web App   │  │  Mobile App │  │   API Tool  │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway Layer                          │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              REST API Controllers                       │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │UserController│  │Notification │  │  Health     │    │    │
│  │  │             │  │ Controller  │  │ Controller  │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Business Logic Layer                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    Service Layer                        │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │UserService  │  │Notification │  │  Validation │    │    │
│  │  │             │  │  Service    │  │   Service   │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Message Queue Layer                        │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    Apache Kafka                         │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │Notification │  │   Producer  │  │  Consumer   │    │    │
│  │  │   Topic     │  │             │  │             │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Data Storage Layer                         │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                     MongoDB                             │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │   Users     │  │Notification │  │   Audit     │    │    │
│  │  │ Collection  │  │ Collection  │  │ Collection  │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      External Services                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ Email       │  │ SMS         │  │ Push        │            │
│  │ Service     │  │ Service     │  │ Service     │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────────────────────────────────────────────────┘
```

### 상세 컴포넌트 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                      │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    Web Layer                            │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │UserController│  │Notification │  │  Exception  │    │    │
│  │  │             │  │ Controller  │  │  Handler    │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                │                                │
│                                ▼                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                  Service Layer                          │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │UserService  │  │Notification │  │  Validation │    │    │
│  │  │             │  │  Service    │  │   Service   │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                │                                │
│                                ▼                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                 Repository Layer                        │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │UserRepository│  │Notification │  │  Mongo      │    │    │
│  │  │             │  │ Repository  │  │  Template   │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                │                                │
│                                ▼                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                  Kafka Layer                            │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │Notification │  │Notification │  │  Kafka      │    │    │
│  │  │  Producer   │  │  Consumer   │  │  Template   │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🔧 컴포넌트 설명

### 1. Web Layer (Controller)

#### UserController
- **역할**: 사용자 관리 API 엔드포인트 제공
- **주요 기능**:
  - 사용자 생성, 조회, 수정, 삭제
  - 활성 사용자 목록 조회
  - 사용자 정보 검증

#### NotificationController
- **역할**: 알림 전송 API 엔드포인트 제공
- **주요 기능**:
  - 특정 사용자에게 알림 전송
  - 모든 사용자에게 일괄 알림 전송
  - 알림 히스토리 조회

### 2. Service Layer

#### UserService
- **역할**: 사용자 관련 비즈니스 로직 처리
- **주요 기능**:
  - 사용자 데이터 검증
  - 중복 이메일 확인
  - 사용자 상태 관리

#### NotificationService
- **역할**: 알림 전송 비즈니스 로직 처리
- **주요 기능**:
  - 알림 메시지 생성
  - Kafka Producer 호출
  - 알림 상태 관리

### 3. Repository Layer

#### UserRepository
- **역할**: 사용자 데이터 접근 계층
- **주요 기능**:
  - MongoDB 사용자 컬렉션 CRUD
  - 사용자 검색 쿼리
  - 인덱스 활용

#### NotificationRepository
- **역할**: 알림 데이터 접근 계층
- **주요 기능**:
  - MongoDB 알림 컬렉션 CRUD
  - 알림 상태별 조회
  - 알림 히스토리 관리

### 4. Kafka Layer

#### NotificationProducer
- **역할**: Kafka 토픽에 알림 메시지 발행
- **주요 기능**:
  - 메시지 직렬화
  - 비동기 메시지 전송
  - 전송 결과 콜백 처리

#### NotificationConsumer
- **역할**: Kafka 토픽에서 알림 메시지 소비
- **주요 기능**:
  - 메시지 역직렬화
  - 실제 알림 전송 처리
  - 에러 처리 및 재시도

## 🔄 데이터 플로우

### 1. 사용자 생성 플로우

```
1. Client → UserController.createUser()
2. UserController → UserService.createUser()
3. UserService → UserRepository.save()
4. UserRepository → MongoDB (users collection)
5. MongoDB → UserRepository → UserService → UserController → Client
```

### 2. 알림 전송 플로우

```
1. Client → NotificationController.sendNotification()
2. NotificationController → NotificationService.sendNotificationToUser()
3. NotificationService → UserRepository.findById() (사용자 정보 조회)
4. NotificationService → NotificationProducer.sendNotification()
5. NotificationProducer → Kafka Topic (notifications)
6. NotificationConsumer ← Kafka Topic (notifications)
7. NotificationConsumer → processNotification()
8. NotificationConsumer → NotificationRepository.save() (상태 업데이트)
9. NotificationConsumer → External Service (Email/SMS/Push)
```

### 3. 대량 알림 전송 플로우

```
1. Client → NotificationController.sendNotificationToAll()
2. NotificationController → NotificationService.sendNotificationToAllUsers()
3. NotificationService → UserRepository.findByActiveTrue()
4. For each user:
   5. NotificationService → NotificationProducer.sendNotification()
   6. NotificationProducer → Kafka Topic (notifications)
7. Multiple NotificationConsumers ← Kafka Topic (notifications)
8. Each Consumer → processNotification() (병렬 처리)
```

## 🛠️ 기술 스택

### Backend Framework
- **Spring Boot 3.5.3**: 웹 애플리케이션 프레임워크
- **Spring Data MongoDB**: MongoDB 데이터 접근
- **Spring Kafka**: Kafka 메시징 통합
- **Spring Web**: REST API 구현

### Database
- **MongoDB 4.4+**: NoSQL 문서 데이터베이스
- **MongoDB Driver**: Java MongoDB 드라이버

### Message Queue
- **Apache Kafka 2.8+**: 분산 메시징 시스템
- **Zookeeper**: Kafka 클러스터 조정

### Development Tools
- **Java 17**: 프로그래밍 언어
- **Gradle 8.0+**: 빌드 도구
- **Lombok**: 보일러플레이트 코드 제거

### Monitoring & Logging
- **SLF4J**: 로깅 프레임워크
- **Spring Boot Actuator**: 애플리케이션 모니터링

## 📈 확장성 고려사항

### 1. 수평 확장

#### 애플리케이션 인스턴스 확장
```yaml
# Docker Compose 예시
services:
  notification-app:
    image: notification-system:latest
    scale: 3  # 3개 인스턴스 실행
    environment:
      - SPRING_KAFKA_CONSUMER_GROUP_ID=notification-group
```

#### Kafka 파티션 확장
```bash
# 파티션 수 증가
bin/kafka-topics.sh --alter \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --partitions 6
```

#### MongoDB 샤딩
```javascript
// MongoDB 샤딩 설정
sh.enableSharding("notificationdb")
sh.shardCollection("notificationdb.users", {"_id": "hashed"})
sh.shardCollection("notificationdb.notifications", {"userId": "hashed"})
```

### 2. 성능 최적화

#### 캐싱 전략
```java
@Cacheable("users")
public User getUserById(String id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));
}
```

#### 배치 처리
```java
@Transactional
public void processBatchNotifications(List<NotificationMessage> messages) {
    // 배치로 알림 처리
    for (NotificationMessage message : messages) {
        notificationProducer.sendNotification(message);
    }
}
```

#### 연결 풀 최적화
```properties
# MongoDB 연결 풀
spring.data.mongodb.uri=mongodb://localhost:27017/notificationdb?maxPoolSize=100&minPoolSize=5

# Kafka Producer 풀
spring.kafka.producer.batch-size=32768
spring.kafka.producer.buffer-memory=67108864
```

### 3. 장애 복구

#### Circuit Breaker 패턴
```java
@CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSendNotification")
public void sendNotification(NotificationMessage message) {
    // 알림 전송 로직
}

public void fallbackSendNotification(NotificationMessage message, Exception ex) {
    // 장애 시 대체 로직
    log.error("알림 전송 실패, 대체 처리: {}", ex.getMessage());
}
```

#### 재시도 메커니즘
```java
@Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public void sendNotificationWithRetry(NotificationMessage message) {
    // 재시도 가능한 알림 전송 로직
}
```

#### 백업 및 복구
```bash
# MongoDB 백업
mongodump --db notificationdb --out /backup

# Kafka 토픽 백업
bin/kafka-console-consumer.sh \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --from-beginning \
  --output-dir /backup/notifications
```

## 🔒 보안 고려사항

### 1. 인증 및 권한
- JWT 토큰 기반 인증 (향후 구현)
- Role-based Access Control (RBAC)
- API 키 인증

### 2. 데이터 보안
- MongoDB 연결 암호화
- Kafka 메시지 암호화
- 민감 정보 마스킹

### 3. 네트워크 보안
- HTTPS/TLS 적용
- 방화벽 설정
- VPN 접근 제어

## 📊 모니터링 및 로깅

### 1. 애플리케이션 모니터링
- Spring Boot Actuator
- Custom Metrics
- Health Checks

### 2. 인프라 모니터링
- MongoDB 성능 모니터링
- Kafka 클러스터 모니터링
- 시스템 리소스 모니터링

### 3. 로깅 전략
- 구조화된 로깅 (JSON)
- 로그 레벨별 관리
- 로그 집계 및 분석 