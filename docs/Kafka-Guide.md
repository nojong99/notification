# Kafka 가이드 (Kafka Guide)

Apache Kafka를 사용한 메시지 큐 및 스트리밍 처리 방법에 대한 상세 가이드입니다.

## 📋 목차

- [Kafka 개요](#kafka-개요)
- [설치 및 설정](#설치-및-설정)
- [Spring Kafka](#spring-kafka)
- [Producer & Consumer](#producer--consumer)
- [토픽 관리](#토픽-관리)
- [성능 최적화](#성능-최적화)
- [모니터링](#모니터링)
- [트러블슈팅](#트러블슈팅)

## 📨 Kafka 개요

### Kafka란?

Apache Kafka는 고성능 분산 메시징 시스템으로, 실시간 데이터 스트리밍과 이벤트 처리에 특화되어 있습니다.

### 주요 특징

- **고성능**: 초당 수십만 메시지 처리
- **확장성**: 수평 확장 가능한 분산 아키텍처
- **내구성**: 메시지가 디스크에 저장되어 손실 방지
- **순서 보장**: 파티션 내에서 메시지 순서 보장
- **실시간성**: 낮은 지연시간으로 실시간 처리

### Kafka vs 기존 메시징 시스템

| 특징 | Kafka | RabbitMQ | ActiveMQ |
|------|-------|----------|----------|
| 처리량 | 매우 높음 | 중간 | 중간 |
| 지연시간 | 낮음 | 매우 낮음 | 낮음 |
| 내구성 | 높음 | 중간 | 중간 |
| 확장성 | 높음 | 중간 | 낮음 |
| 순서 보장 | 파티션 내 | 큐 내 | 큐 내 |

### Kafka 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Producer      │    │   Kafka         │    │   Consumer      │
│   (발행자)      │───▶│   Broker        │───▶│   (소비자)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Topic         │    │   Partition     │    │   Consumer      │
│   (토픽)        │    │   (파티션)      │    │   Group         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## ⚙️ 설치 및 설정

### 1. Kafka 설치

#### 모든 플랫폼
```bash
# 1. Kafka 다운로드
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz

# 2. 압축 해제
tar -xzf kafka_2.13-3.6.0.tgz
cd kafka_2.13-3.6.0

# 3. 환경 변수 설정
export KAFKA_HOME=/path/to/kafka_2.13-3.6.0
export PATH=$PATH:$KAFKA_HOME/bin
```

### 2. Zookeeper 시작

```bash
# Zookeeper 시작 (백그라운드)
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties

# Zookeeper 상태 확인
echo stat | nc localhost 2181
```

### 3. Kafka Broker 시작

```bash
# Kafka 시작 (백그라운드)
bin/kafka-server-start.sh -daemon config/server.properties

# Kafka 상태 확인
bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

### 4. 토픽 생성

```bash
# 알림 토픽 생성
bin/kafka-topics.sh --create \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# 토픽 목록 확인
bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# 토픽 상세 정보 확인
bin/kafka-topics.sh --describe \
  --topic notifications \
  --bootstrap-server localhost:9092
```

### 5. Spring Boot 설정

#### application.properties
```properties
# Kafka 브로커 설정
spring.kafka.bootstrap-servers=localhost:9092

# Producer 설정
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.batch-size=16384
spring.kafka.producer.buffer-memory=33554432

# Consumer 설정
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.enable-auto-commit=true
spring.kafka.consumer.auto-commit-interval=1000
spring.kafka.consumer.max-poll-records=500
```

#### build.gradle
```gradle
dependencies {
    implementation 'org.springframework.kafka:spring-kafka'
}
```

## 🔧 Spring Kafka

### 1. 기본 설정

#### KafkaConfig 클래스
```java
@Configuration
public class KafkaConfig {
    
    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name("notifications")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
            ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
```

### 2. Producer 설정

#### 기본 Producer
```java
@Component
public class NotificationProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String TOPIC = "notifications";
    
    public NotificationProducer(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    public void sendNotification(NotificationMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            
            // 동기 전송
            kafkaTemplate.send(TOPIC, message.getUserId(), messageJson);
            
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 오류: {}", e.getMessage());
        }
    }
    
    public void sendNotificationAsync(NotificationMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            
            // 비동기 전송
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(TOPIC, message.getUserId(), messageJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("메시지 전송 성공: topic={}, partition={}, offset={}", 
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("메시지 전송 실패: {}", ex.getMessage());
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 오류: {}", e.getMessage());
        }
    }
}
```

#### 고급 Producer 설정
```java
@Configuration
public class KafkaProducerConfig {
    
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}
```

### 3. Consumer 설정

#### 기본 Consumer
```java
@Component
public class NotificationConsumer {
    
    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    
    public NotificationConsumer(ObjectMapper objectMapper,
                               NotificationRepository notificationRepository) {
        this.objectMapper = objectMapper;
        this.notificationRepository = notificationRepository;
    }
    
    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void consumeNotification(String message) {
        try {
            NotificationMessage notificationMessage = 
                objectMapper.readValue(message, NotificationMessage.class);
            
            log.info("알림 메시지 수신: {}", notificationMessage);
            
            // 알림 처리 로직
            processNotification(notificationMessage);
            
        } catch (Exception e) {
            log.error("알림 메시지 처리 오류: {}", e.getMessage());
        }
    }
    
    private void processNotification(NotificationMessage message) {
        try {
            // 실제 알림 전송 로직
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
            Thread.sleep(1000);
            
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
```

#### 고급 Consumer 설정
```java
@Configuration
public class KafkaConsumerConfig {
    
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 동시 Consumer 수
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }
}
```

## 📊 Producer & Consumer

### 1. 메시지 발행 (Producer)

#### 동기 전송
```java
public void sendMessageSync(String topic, String key, String message) {
    try {
        SendResult<String, String> result = kafkaTemplate.send(topic, key, message).get();
        log.info("메시지 전송 성공: topic={}, partition={}, offset={}", 
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
    } catch (Exception e) {
        log.error("메시지 전송 실패: {}", e.getMessage());
    }
}
```

#### 비동기 전송
```java
public void sendMessageAsync(String topic, String key, String message) {
    CompletableFuture<SendResult<String, String>> future = 
        kafkaTemplate.send(topic, key, message);
    
    future.whenComplete((result, ex) -> {
        if (ex == null) {
            log.info("메시지 전송 성공: {}", result.getRecordMetadata().offset());
        } else {
            log.error("메시지 전송 실패: {}", ex.getMessage());
        }
    });
}
```

#### 배치 전송
```java
public void sendBatchMessages(List<NotificationMessage> messages) {
    List<CompletableFuture<SendResult<String, String>>> futures = new ArrayList<>();
    
    for (NotificationMessage message : messages) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send("notifications", message.getUserId(), messageJson);
            futures.add(future);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 오류: {}", e.getMessage());
        }
    }
    
    // 모든 전송 완료 대기
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("배치 메시지 전송 완료: {}건", messages.size());
            } else {
                log.error("배치 메시지 전송 실패: {}", ex.getMessage());
            }
        });
}
```

### 2. 메시지 소비 (Consumer)

#### 기본 Consumer
```java
@KafkaListener(topics = "notifications", groupId = "notification-group")
public void consumeMessage(String message) {
    log.info("메시지 수신: {}", message);
    // 메시지 처리 로직
}
```

#### 배치 Consumer
```java
@KafkaListener(topics = "notifications", groupId = "notification-group")
public void consumeBatchMessages(List<String> messages) {
    log.info("배치 메시지 수신: {}건", messages.size());
    
    for (String message : messages) {
        try {
            NotificationMessage notificationMessage = 
                objectMapper.readValue(message, NotificationMessage.class);
            processNotification(notificationMessage);
        } catch (Exception e) {
            log.error("메시지 처리 오류: {}", e.getMessage());
        }
    }
}
```

#### 에러 처리 Consumer
```java
@KafkaListener(topics = "notifications", groupId = "notification-group")
public void consumeMessageWithErrorHandling(String message) {
    try {
        NotificationMessage notificationMessage = 
            objectMapper.readValue(message, NotificationMessage.class);
        processNotification(notificationMessage);
    } catch (Exception e) {
        log.error("메시지 처리 실패: {}", e.getMessage());
        
        // 실패한 메시지를 다른 토픽으로 전송
        kafkaTemplate.send("notification-failed", message);
    }
}
```

### 3. Consumer Group

#### Consumer Group 동작
```java
// 같은 그룹의 Consumer들이 파티션을 나누어 처리
@KafkaListener(topics = "notifications", groupId = "notification-group-1")
public void consumeGroup1(String message) {
    log.info("Group 1 - 메시지 수신: {}", message);
}

@KafkaListener(topics = "notifications", groupId = "notification-group-1")
public void consumeGroup1Second(String message) {
    log.info("Group 1 Second - 메시지 수신: {}", message);
}

// 다른 그룹의 Consumer
@KafkaListener(topics = "notifications", groupId = "notification-group-2")
public void consumeGroup2(String message) {
    log.info("Group 2 - 메시지 수신: {}", message);
}
```

## 🎯 토픽 관리

### 1. 토픽 생성 및 관리

#### 토픽 생성
```bash
# 기본 토픽 생성
bin/kafka-topics.sh --create \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# 고급 설정으로 토픽 생성
bin/kafka-topics.sh --create \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --partitions 6 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config segment.bytes=1073741824 \
  --config cleanup.policy=delete
```

#### 토픽 설정 확인
```bash
# 토픽 상세 정보
bin/kafka-topics.sh --describe \
  --topic notifications \
  --bootstrap-server localhost:9092

# 토픽 설정 변경
bin/kafka-configs.sh --bootstrap-server localhost:9092 \
  --entity-type topics \
  --entity-name notifications \
  --alter \
  --add-config retention.ms=86400000
```

### 2. 파티션 관리

#### 파티션 수 증가
```bash
# 파티션 수 증가 (감소는 불가능)
bin/kafka-topics.sh --alter \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --partitions 6
```

#### 파티션 할당 확인
```bash
# 파티션 할당 정보
bin/kafka-topics.sh --describe \
  --topic notifications \
  --bootstrap-server localhost:9092

# Consumer Group의 파티션 할당
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group notification-group
```

### 3. 토픽 모니터링

#### 토픽 통계
```bash
# 토픽별 메시지 수
bin/kafka-run-class.sh kafka.tools.GetOffsetShell \
  --bootstrap-server localhost:9092 \
  --topic notifications

# Consumer Group 오프셋
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group notification-group
```

## ⚡ 성능 최적화

### 1. Producer 최적화

#### 배치 크기 최적화
```properties
# application.properties
spring.kafka.producer.batch-size=32768
spring.kafka.producer.buffer-memory=67108864
spring.kafka.producer.compression-type=snappy
spring.kafka.producer.linger.ms=10
```

#### 압축 설정
```java
@Bean
public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    // ... 기본 설정
    
    // 압축 설정
    configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
    configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);
    configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
    
    return new DefaultKafkaProducerFactory<>(configProps);
}
```

### 2. Consumer 최적화

#### 병렬 처리
```java
@Configuration
public class KafkaConsumerConfig {
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(6); // 동시 Consumer 수 (파티션 수와 동일하게)
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }
}
```

#### 배치 처리
```properties
# application.properties
spring.kafka.consumer.max-poll-records=1000
spring.kafka.consumer.fetch-min-bytes=1024
spring.kafka.consumer.fetch-max-wait-ms=500
```

### 3. 토픽 최적화

#### 파티션 수 최적화
```bash
# 처리량에 따른 파티션 수 계산
# 파티션 수 = (초당 메시지 수 * 메시지 크기) / (초당 처리 가능한 바이트 수)

# 예: 초당 10,000 메시지, 메시지 크기 1KB, 초당 10MB 처리 가능
# 파티션 수 = (10,000 * 1,024) / (10 * 1,048,576) ≈ 1
```

#### 보존 정책 설정
```bash
# 메시지 보존 기간 설정 (7일)
bin/kafka-configs.sh --bootstrap-server localhost:9092 \
  --entity-type topics \
  --entity-name notifications \
  --alter \
  --add-config retention.ms=604800000

# 세그먼트 크기 설정 (1GB)
bin/kafka-configs.sh --bootstrap-server localhost:9092 \
  --entity-type topics \
  --entity-name notifications \
  --alter \
  --add-config segment.bytes=1073741824
```

## 📊 모니터링

### 1. Kafka 모니터링

#### 브로커 상태 확인
```bash
# 브로커 정보
bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# 브로커 메트릭
bin/kafka-run-class.sh kafka.tools.JmxTool \
  --object-name kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec \
  --jmx-url service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi
```

#### 토픽 모니터링
```bash
# 토픽별 메시지 수
bin/kafka-run-class.sh kafka.tools.GetOffsetShell \
  --bootstrap-server localhost:9092 \
  --topic notifications

# Consumer Group 상태
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group notification-group
```

### 2. Spring Boot 모니터링

#### Actuator 설정
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

```properties
# application.properties
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

#### 커스텀 메트릭
```java
@Component
public class KafkaMetrics {
    private final MeterRegistry meterRegistry;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public KafkaMetrics(MeterRegistry meterRegistry, KafkaTemplate<String, String> kafkaTemplate) {
        this.meterRegistry = meterRegistry;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @EventListener
    public void handleKafkaEvent(KafkaEvent event) {
        if (event instanceof SendResult) {
            meterRegistry.counter("kafka.messages.sent").increment();
        }
    }
}
```

### 3. 로깅 설정

#### Kafka 로깅
```properties
# application.properties
logging.level.org.apache.kafka=INFO
logging.level.org.springframework.kafka=DEBUG
logging.level.com.example.demo.kafka=DEBUG
```

#### 로그 패턴
```properties
# application.properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

## 🔧 트러블슈팅

### 1. 일반적인 문제

#### 연결 문제
```bash
# Kafka 브로커 상태 확인
bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# 포트 확인
netstat -tlnp | grep 9092

# 로그 확인
tail -f logs/server.log
```

#### Consumer 지연 문제
```bash
# Consumer Group 상태 확인
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group notification-group

# 오프셋 리셋
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group notification-group \
  --topic notifications \
  --reset-offsets --to-earliest --execute
```

### 2. 성능 문제

#### Producer 성능
```properties
# application.properties
# 배치 크기 증가
spring.kafka.producer.batch-size=65536

# 버퍼 메모리 증가
spring.kafka.producer.buffer-memory=134217728

# 압축 활성화
spring.kafka.producer.compression-type=snappy
```

#### Consumer 성능
```properties
# application.properties
# 폴링 간격 조정
spring.kafka.consumer.max-poll-records=1000

# 페치 크기 조정
spring.kafka.consumer.fetch-min-bytes=1024
spring.kafka.consumer.fetch-max-wait-ms=500
```

### 3. 디버깅

#### 메시지 전송 확인
```bash
# 토픽에서 메시지 확인
bin/kafka-console-consumer.sh \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --from-beginning

# 메시지 전송 테스트
bin/kafka-console-producer.sh \
  --topic notifications \
  --bootstrap-server localhost:9092
```

#### Consumer 로그 확인
```bash
# Consumer Group 오프셋 확인
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group notification-group

# 토픽 파티션 정보
bin/kafka-topics.sh --describe \
  --topic notifications \
  --bootstrap-server localhost:9092
```

### 4. 백업 및 복구

#### 토픽 백업
```bash
# 토픽 데이터 백업
bin/kafka-console-consumer.sh \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --from-beginning \
  --output-dir /backup/notifications

# 토픽 설정 백업
bin/kafka-configs.sh --bootstrap-server localhost:9092 \
  --entity-type topics \
  --entity-name notifications \
  --describe > /backup/notifications-config.txt
```

## 📚 추가 리소스

- [Apache Kafka 공식 문서](https://kafka.apache.org/documentation/)
- [Spring Kafka 문서](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Confluent Platform](https://docs.confluent.io/) - Kafka 기업용 배포판
- [Kafka UI](https://github.com/provectus/kafka-ui) - Kafka 웹 관리 도구 