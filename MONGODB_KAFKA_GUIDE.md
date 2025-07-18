# MongoDB & Kafka 사용법 및 원리 가이드

## MongoDB 사용법 및 원리

### 1. MongoDB 설정

#### application.properties 설정
```properties
# MongoDB 연결 설정
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=notificationdb
```

#### 의존성 추가
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
```

### 2. MongoDB 사용 원리

#### 2.1 Spring Data MongoDB 자동 설정
- Spring Boot가 `spring-boot-starter-data-mongodb` 의존성을 감지하면 자동으로 MongoDB 연결을 설정
- `MongoTemplate`과 `MongoRepository` 빈들이 자동으로 생성됨
- MongoDB 드라이버가 자동으로 설정되어 연결 풀 관리

#### 2.2 Document 매핑 원리
```java
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    private boolean active = true;
}
```

**매핑 어노테이션:**
- `@Document(collection = "users")`: MongoDB 컬렉션과 매핑
- `@Id`: MongoDB의 `_id` 필드와 매핑 (자동 생성)
- `@Field`: 특정 필드명 매핑 (기본값: 변수명)

#### 2.3 Repository 패턴 원리
```java
public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByActiveTrue();
    User findByEmail(String email);
    List<User> findByUsernameAndActive(String username, boolean active);
}
```

**Repository 동작 원리:**
1. **기본 CRUD 메서드**: `MongoRepository`에서 상속받은 메서드들
   - `save()`, `findById()`, `findAll()`, `deleteById()` 등

2. **쿼리 메서드**: 메서드 이름으로 쿼리 자동 생성
   - `findByActiveTrue()` → `{active: true}`
   - `findByEmail(String email)` → `{email: ?0}`
   - `findByUsernameAndActive()` → `{username: ?0, active: ?1}`

3. **쿼리 생성 규칙:**
   - `findBy` + 필드명: 해당 필드로 조회
   - `And`, `Or`: 조건 결합
   - `True`, `False`: boolean 값 조회
   - `OrderBy` + 필드명: 정렬

#### 2.4 데이터 저장 과정
```
Java 객체 → JSON 직렬화 → MongoDB BSON → 컬렉션 저장
```

1. **객체 생성**: Java 객체 생성
2. **JSON 변환**: Jackson 라이브러리로 JSON 직렬화
3. **BSON 변환**: MongoDB가 JSON을 BSON(Binary JSON)으로 변환
4. **저장**: 지정된 컬렉션에 문서 저장

#### 2.5 MongoDB 장점
- **스키마 유연성**: 문서 구조를 자유롭게 변경 가능
- **수평 확장**: 샤딩을 통한 대용량 데이터 처리
- **복잡한 쿼리**: 중첩 문서, 배열 쿼리 지원
- **JSON 친화적**: JavaScript/JSON과 유사한 구조

---

## Kafka 사용법 및 원리

### 1. Kafka 설정

#### application.properties 설정
```properties
# Kafka 브로커 설정
spring.kafka.bootstrap-servers=localhost:9092

# Consumer 설정
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer

# Producer 설정
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

#### 의존성 추가
```gradle
implementation 'org.springframework.kafka:spring-kafka'
```

### 2. Kafka 사용 원리

#### 2.1 Kafka 아키텍처
```
Producer → Kafka Broker → Consumer
    ↓         ↓           ↓
  메시지    토픽/파티션    메시지
  발행      저장          소비
```

**핵심 구성요소:**
- **Producer**: 메시지를 발행하는 애플리케이션
- **Consumer**: 메시지를 소비하는 애플리케이션
- **Broker**: Kafka 서버 (메시지 저장 및 전달)
- **Topic**: 메시지의 논리적 그룹
- **Partition**: 토픽을 나누는 물리적 단위

#### 2.2 Producer 원리
```java
@Component
public class NotificationProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public void sendNotification(NotificationMessage message) {
        String messageJson = objectMapper.writeValueAsString(message);
        kafkaTemplate.send("notifications", message.getUserId(), messageJson);
    }
}
```

**Producer 동작 과정:**
1. **메시지 직렬화**: Java 객체를 JSON 문자열로 변환
2. **토픽 선택**: 메시지를 보낼 토픽 지정
3. **파티션 결정**: Key 기반으로 파티션 선택 (같은 Key는 같은 파티션)
4. **브로커 전송**: 네트워크를 통해 Kafka 브로커로 전송
5. **비동기 응답**: 메시지 전송 후 즉시 응답 (성능 최적화)

#### 2.3 Consumer 원리
```java
@Component
public class NotificationConsumer {
    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void consumeNotification(String message) {
        NotificationMessage notificationMessage = objectMapper.readValue(message, NotificationMessage.class);
        processNotification(notificationMessage);
    }
}
```

**Consumer 동작 과정:**
1. **토픽 구독**: `@KafkaListener`로 특정 토픽 구독
2. **메시지 폴링**: 브로커에서 메시지를 주기적으로 가져옴
3. **메시지 역직렬화**: JSON 문자열을 Java 객체로 변환
4. **비즈니스 로직 실행**: 실제 알림 처리 로직 수행
5. **Offset 커밋**: 메시지 처리 완료 후 Offset 업데이트

#### 2.4 메시지 처리 흐름
```
API 요청 → NotificationService → NotificationProducer → Kafka 토픽 → NotificationConsumer → 알림 전송
    ↓              ↓                    ↓                    ↓                ↓
  HTTP 요청    비즈니스 로직        메시지 발행          메시지 저장      메시지 소비
```

#### 2.5 Consumer Group 원리
```java
@KafkaListener(topics = "notifications", groupId = "notification-group")
```

**Consumer Group 동작:**
- **그룹 내 분산**: 같은 그룹의 Consumer들이 파티션을 나누어 처리
- **확장성**: Consumer 인스턴스 추가로 처리량 증가
- **장애 복구**: Consumer 장애 시 다른 Consumer가 파티션 인계
- **순서 보장**: 파티션 내에서 메시지 순서 보장

#### 2.6 Offset 관리
- **Offset**: Consumer가 읽은 메시지의 위치 정보
- **auto.offset.reset**: 
  - `earliest`: 처음부터 읽기
  - `latest`: 최신 메시지부터 읽기
- **자동 커밋**: 메시지 처리 완료 후 자동으로 Offset 업데이트

#### 2.7 Kafka 장점
- **고성능**: 초당 수십만 메시지 처리 가능
- **확장성**: 브로커 추가로 처리량 증가
- **내구성**: 메시지가 디스크에 저장되어 손실 방지
- **순서 보장**: 파티션 내에서 메시지 순서 보장
- **장애 복구**: Consumer 장애 시 메시지 재처리 가능
- **비동기 처리**: Producer와 Consumer의 느슨한 결합

### 3. 실제 사용 시나리오

#### 3.1 알림 시스템에서의 활용
```
사용자 액션 → 알림 요청 → Kafka 큐 → 알림 처리 → 실제 전송
```

1. **사용자 액션**: 로그인, 주문, 결제 등
2. **알림 요청**: API를 통해 알림 전송 요청
3. **Kafka 큐**: 메시지를 큐에 저장 (비동기 처리)
4. **알림 처리**: Consumer가 메시지를 받아 처리
5. **실제 전송**: 이메일, SMS, 푸시 알림 전송

#### 3.2 장점
- **시스템 분리**: 알림 요청과 실제 전송 로직 분리
- **부하 분산**: 대량 알림도 순차적으로 처리
- **장애 격리**: 알림 시스템 장애가 메인 시스템에 영향 없음
- **확장성**: Consumer 인스턴스 추가로 처리량 증가 