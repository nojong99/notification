# 사용자 알림 시스템

MongoDB와 Kafka를 사용한 사용자 알림 시스템입니다.

## 기술 스택

- **Spring Boot 3.5.3**
- **MongoDB** - 사용자 및 알림 데이터 저장
- **Apache Kafka** - 메시지 큐 및 알림 처리
- **Java 17**

## 프로젝트 구조

```
src/main/java/com/example/demo/
├── config/
│   └── KafkaConfig.java          # Kafka 설정
├── controller/
│   ├── UserController.java       # 사용자 REST API
│   └── NotificationController.java # 알림 REST API
├── dto/
│   └── NotificationMessage.java  # Kafka 메시지 DTO
├── kafka/
│   ├── NotificationProducer.java # Kafka Producer
│   └── NotificationConsumer.java # Kafka Consumer
├── model/
│   ├── User.java                 # 사용자 모델
│   └── Notification.java         # 알림 모델
├── repository/
│   ├── UserRepository.java       # 사용자 Repository
│   └── NotificationRepository.java # 알림 Repository
├── service/
│   ├── UserService.java          # 사용자 서비스
│   └── NotificationService.java  # 알림 서비스
└── PlayApplication.java          # 메인 애플리케이션
```

## 실행 방법

### 1. MongoDB 설치 및 실행
```bash
# MongoDB 설치 (Windows)
# https://www.mongodb.com/try/download/community 에서 다운로드

# MongoDB 서비스 시작
mongod
```

### 2. Kafka 설치 및 실행
```bash
# Kafka 다운로드
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz
tar -xzf kafka_2.13-3.6.0.tgz
cd kafka_2.13-3.6.0

# Zookeeper 시작
bin/zookeeper-server-start.sh config/zookeeper.properties

# Kafka 시작 (새 터미널에서)
bin/kafka-server-start.sh config/server.properties
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

## API 사용법

### 사용자 관리

#### 사용자 생성
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "phoneNumber": "010-1234-5678"
  }'
```

#### 모든 사용자 조회
```bash
curl http://localhost:8080/api/users
```

#### 활성 사용자 조회
```bash
curl http://localhost:8080/api/users/active
```

### 알림 관리

#### 특정 사용자에게 알림 전송
```bash
curl -X POST "http://localhost:8080/api/notifications/send/user/{userId}?title=테스트&message=안녕하세요&type=EMAIL"
```

#### 모든 사용자에게 알림 전송
```bash
curl -X POST "http://localhost:8080/api/notifications/send/all?title=공지사항&message=시스템 점검이 있습니다&type=SMS"
```

#### 사용자별 알림 조회
```bash
curl http://localhost:8080/api/notifications/user/{userId}
```

#### 상태별 알림 조회
```bash
curl http://localhost:8080/api/notifications/status/SENT
```

## 알림 타입

- `EMAIL` - 이메일 알림
- `SMS` - SMS 알림  
- `PUSH` - 푸시 알림

## 알림 상태

- `PENDING` - 대기 중
- `SENT` - 전송 완료
- `FAILED` - 전송 실패 