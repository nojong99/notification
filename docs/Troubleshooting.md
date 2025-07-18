# 트러블슈팅 (Troubleshooting)

사용자 알림 시스템에서 발생할 수 있는 문제들과 해결 방법을 설명합니다.

## 📋 목차

- [일반적인 문제](#일반적인-문제)
- [MongoDB 문제](#mongodb-문제)
- [Kafka 문제](#kafka-문제)
- [Spring Boot 문제](#spring-boot-문제)
- [성능 문제](#성능-문제)
- [네트워크 문제](#네트워크-문제)

## ❗ 일반적인 문제

### 1. 포트 충돌

#### 문제 증상
```
BindException: Address already in use
```

#### 해결 방법
```bash
# 포트 사용 중인 프로세스 확인
netstat -tlnp | grep 8080
netstat -tlnp | grep 27017
netstat -tlnp | grep 9092

# 프로세스 종료
sudo kill -9 <PID>

# 또는 다른 포트 사용
# application.properties
server.port=8081
```

### 2. 메모리 부족

#### 문제 증상
```
OutOfMemoryError: Java heap space
```

#### 해결 방법
```bash
# JVM 힙 메모리 증가
export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC"

# 애플리케이션 재시작
java $JAVA_OPTS -jar notification-system.jar
```

### 3. 권한 문제

#### 문제 증상
```
Permission denied
```

#### 해결 방법
```bash
# 파일 권한 확인 및 수정
chmod +x gradlew
chmod 755 logs/
chown -R user:group /path/to/application

# MongoDB 데이터 디렉토리 권한
sudo chown -R mongodb:mongodb /var/lib/mongodb
sudo chmod 755 /var/lib/mongodb
```

## 🗄️ MongoDB 문제

### 1. 연결 실패

#### 문제 증상
```
com.mongodb.MongoSocketOpenException: Exception opening socket
```

#### 해결 방법
```bash
# MongoDB 서비스 상태 확인
sudo systemctl status mongod

# MongoDB 서비스 시작
sudo systemctl start mongod
sudo systemctl enable mongod

# 연결 테스트
mongosh --eval "db.runCommand('ping')"

# 방화벽 설정 확인
sudo ufw status
sudo ufw allow 27017
```

#### 설정 확인
```properties
# application.properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=notificationdb

# 연결 풀 설정
spring.data.mongodb.uri=mongodb://localhost:27017/notificationdb?maxPoolSize=100&minPoolSize=5
```

### 2. 인증 실패

#### 문제 증상
```
Authentication failed
```

#### 해결 방법
```javascript
// MongoDB Shell에서 사용자 생성
use admin
db.createUser({
  user: "admin",
  pwd: "password",
  roles: ["root"]
})

use notificationdb
db.createUser({
  user: "notification_user",
  pwd: "password",
  roles: ["readWrite"]
})
```

```properties
# application.properties
spring.data.mongodb.uri=mongodb://notification_user:password@localhost:27017/notificationdb?authSource=notificationdb
```

### 3. 느린 쿼리

#### 문제 증상
```
쿼리 실행 시간이 오래 걸림
```

#### 해결 방법
```javascript
// 느린 쿼리 로깅 활성화
db.setProfilingLevel(1, 100); // 100ms 이상 쿼리 로깅

// 느린 쿼리 확인
db.system.profile.find().sort({ts: -1}).limit(10)

// 인덱스 생성
db.users.createIndex({email: 1})
db.users.createIndex({active: 1})
db.users.createIndex({createdAt: -1})

// 쿼리 실행 계획 확인
db.users.find({email: "test@example.com"}).explain("executionStats")
```

### 4. 디스크 공간 부족

#### 문제 증상
```
Disk space is full
```

#### 해결 방법
```bash
# 디스크 사용량 확인
df -h

# MongoDB 데이터 크기 확인
mongosh --eval "db.stats()"

# 오래된 데이터 삭제
mongosh --eval "db.notifications.deleteMany({createdAt: {\$lt: new Date(Date.now() - 90*24*60*60*1000)}})"

# 압축
mongosh --eval "db.runCommand({compact: 'notifications'})"
```

## 📨 Kafka 문제

### 1. 브로커 연결 실패

#### 문제 증상
```
Connection refused
```

#### 해결 방법
```bash
# Kafka 서비스 상태 확인
sudo systemctl status kafka

# Kafka 서비스 시작
sudo systemctl start kafka
sudo systemctl enable kafka

# 브로커 상태 확인
bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# Zookeeper 상태 확인
echo stat | nc localhost 2181
```

#### 설정 확인
```properties
# application.properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.auto-offset-reset=earliest
```

### 2. 토픽 생성 실패

#### 문제 증상
```
Topic creation failed
```

#### 해결 방법
```bash
# 토픽 생성
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

### 3. Consumer 지연

#### 문제 증상
```
Consumer lag 증가
```

#### 해결 방법
```bash
# Consumer Group 상태 확인
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group notification-group

# 오프셋 리셋
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group notification-group \
  --topic notifications \
  --reset-offsets --to-earliest --execute

# Consumer 인스턴스 수 증가
# application.properties
spring.kafka.listener.concurrency=6
```

### 4. 메시지 손실

#### 문제 증상
```
메시지가 처리되지 않음
```

#### 해결 방법
```properties
# Producer 설정
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.enable.idempotence=true

# Consumer 설정
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.listener.ack-mode=manual_immediate
```

```java
@KafkaListener(topics = "notifications", groupId = "notification-group")
public void consumeNotification(String message, Acknowledgment ack) {
    try {
        // 메시지 처리
        processNotification(message);
        
        // 수동 커밋
        ack.acknowledge();
        
    } catch (Exception e) {
        log.error("메시지 처리 실패: {}", e.getMessage());
        // 재시도 로직 또는 Dead Letter Queue로 전송
    }
}
```

## 🌱 Spring Boot 문제

### 1. 애플리케이션 시작 실패

#### 문제 증상
```
Application failed to start
```

#### 해결 방법
```bash
# 로그 확인
tail -f logs/notification-system.log

# 포트 확인
netstat -tlnp | grep 8080

# 의존성 확인
./gradlew dependencies

# 빌드 클린
./gradlew clean build
```

#### 일반적인 원인
```properties
# 데이터베이스 연결 실패
spring.data.mongodb.uri=mongodb://localhost:27017/notificationdb

# Kafka 연결 실패
spring.kafka.bootstrap-servers=localhost:9092

# 포트 충돌
server.port=8080
```

### 2. Bean 생성 실패

#### 문제 증상
```
BeanCreationException
```

#### 해결 방법
```java
// 설정 클래스 확인
@Configuration
public class KafkaConfig {
    
    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name("notifications")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

// 의존성 주입 확인
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationProducer notificationProducer;
    private final UserRepository userRepository;
}
```

### 3. 트랜잭션 문제

#### 문제 증상
```
Transaction rolled back
```

#### 해결 방법
```java
@Service
@Transactional
public class UserService {
    
    @Transactional(rollbackFor = Exception.class)
    public User createUser(UserRequest request) {
        try {
            // 비즈니스 로직
            User user = userRepository.save(convertToUser(request));
            return user;
        } catch (Exception e) {
            log.error("사용자 생성 실패: {}", e.getMessage());
            throw new RuntimeException("사용자 생성에 실패했습니다", e);
        }
    }
}
```

## ⚡ 성능 문제

### 1. 응답 시간 지연

#### 문제 증상
```
API 응답이 느림
```

#### 해결 방법
```java
// 캐싱 적용
@Cacheable("users")
public User getUserById(String id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + id));
}

// 비동기 처리
@Async
public CompletableFuture<Void> sendNotificationAsync(NotificationMessage message) {
    // 알림 전송 로직
    return CompletableFuture.completedFuture(null);
}

// 연결 풀 최적화
spring.data.mongodb.uri=mongodb://localhost:27017/notificationdb?maxPoolSize=100&minPoolSize=5
spring.kafka.producer.batch-size=32768
spring.kafka.producer.buffer-memory=67108864
```

### 2. 메모리 사용량 증가

#### 문제 증상
```
메모리 사용량이 계속 증가
```

#### 해결 방법
```bash
# JVM 힙 덤프 생성
jmap -dump:format=b,file=heap.hprof <pid>

# 메모리 누수 분석
jstat -gc <pid> 1000 10

# GC 로깅 활성화
export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
```

### 3. CPU 사용량 증가

#### 문제 증상
```
CPU 사용률이 높음
```

#### 해결 방법
```bash
# 스레드 덤프 생성
jstack <pid> > thread.txt

# CPU 사용량 확인
top -p <pid>

# 프로파일링 도구 사용
# JProfiler, YourKit 등
```

## 🌐 네트워크 문제

### 1. 방화벽 설정

#### 문제 증상
```
Connection timeout
```

#### 해결 방법
```bash
# 방화벽 상태 확인
sudo ufw status

# 필요한 포트 허용
sudo ufw allow 8080/tcp  # 애플리케이션
sudo ufw allow 27017/tcp # MongoDB
sudo ufw allow 9092/tcp  # Kafka
sudo ufw allow 2181/tcp  # Zookeeper

# 방화벽 재시작
sudo ufw reload
```

### 2. DNS 해결 문제

#### 문제 증상
```
UnknownHostException
```

#### 해결 방법
```bash
# DNS 확인
nslookup localhost
ping localhost

# hosts 파일 확인
cat /etc/hosts

# 네트워크 설정 확인
ip addr show
```

### 3. 프록시 설정

#### 문제 증상
```
프록시 환경에서 연결 실패
```

#### 해결 방법
```properties
# application.properties
# HTTP 프록시 설정
spring.http.proxy.host=proxy.example.com
spring.http.proxy.port=8080
spring.http.proxy.username=user
spring.http.proxy.password=password

# HTTPS 프록시 설정
spring.https.proxy.host=proxy.example.com
spring.https.proxy.port=8080
```

## 🔧 디버깅 도구

### 1. 로그 분석

#### 로그 레벨 설정
```properties
# application.properties
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.kafka=DEBUG
logging.level.org.springframework.data.mongodb=DEBUG

# 로그 파일 설정
logging.file.name=logs/notification-system.log
logging.file.max-size=10MB
logging.file.max-history=30
```

#### 로그 패턴 분석
```bash
# 에러 로그 확인
grep "ERROR" logs/notification-system.log

# 특정 사용자 관련 로그
grep "user123" logs/notification-system.log

# 시간별 로그 분석
grep "2024-01-01" logs/notification-system.log
```

### 2. 모니터링 도구

#### Spring Boot Actuator
```properties
# application.properties
management.endpoints.web.exposure.include=health,metrics,info,env
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

```bash
# 헬스체크
curl http://localhost:8080/actuator/health

# 메트릭 확인
curl http://localhost:8080/actuator/metrics

# 환경 변수 확인
curl http://localhost:8080/actuator/env
```

#### MongoDB 모니터링
```javascript
// 서버 상태 확인
db.serverStatus()

// 데이터베이스 통계
db.stats()

// 컬렉션 통계
db.users.stats()
db.notifications.stats()

// 현재 실행 중인 쿼리
db.currentOp()
```

#### Kafka 모니터링
```bash
# 토픽 정보 확인
bin/kafka-topics.sh --describe --topic notifications --bootstrap-server localhost:9092

# Consumer Group 상태 확인
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group notification-group

# 브로커 메트릭
bin/kafka-run-class.sh kafka.tools.JmxTool \
  --object-name kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec \
  --jmx-url service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi
```

### 3. 성능 프로파일링

#### JVM 프로파일링
```bash
# 힙 덤프 생성
jmap -dump:format=b,file=heap.hprof <pid>

# 스레드 덤프 생성
jstack <pid> > thread.txt

# GC 통계 확인
jstat -gc <pid> 1000 10

# JVM 옵션 확인
jinfo <pid>
```

#### 시스템 리소스 모니터링
```bash
# CPU 및 메모리 사용량
top -p <pid>

# 디스크 I/O 확인
iostat -x 1

# 네트워크 사용량 확인
netstat -i

# 프로세스별 리소스 사용량
ps aux | grep java
```

## 📞 지원 및 문의

### 1. 문제 보고

문제를 보고할 때 다음 정보를 포함해주세요:

- **문제 설명**: 발생한 문제의 상세한 설명
- **환경 정보**: OS, Java 버전, MongoDB 버전, Kafka 버전
- **에러 로그**: 관련된 에러 메시지와 스택 트레이스
- **재현 단계**: 문제를 재현하는 단계별 방법
- **예상 동작**: 정상적으로 동작했을 때의 결과

### 2. 로그 수집

```bash
# 애플리케이션 로그
tail -n 1000 logs/notification-system.log

# 시스템 로그
journalctl -u notification-system -n 100

# MongoDB 로그
tail -n 1000 /var/log/mongodb/mongod.log

# Kafka 로그
tail -n 1000 /opt/kafka/logs/server.log
```

### 3. 진단 도구

#### 시스템 진단 스크립트
```bash
#!/bin/bash
# diagnose.sh

echo "=== 시스템 정보 ==="
uname -a
java -version
echo

echo "=== 프로세스 상태 ==="
ps aux | grep -E "(java|mongod|kafka)" | grep -v grep
echo

echo "=== 포트 사용 상태 ==="
netstat -tlnp | grep -E "(8080|27017|9092|2181)"
echo

echo "=== 디스크 사용량 ==="
df -h
echo

echo "=== 메모리 사용량 ==="
free -h
echo

echo "=== 로그 파일 크기 ==="
ls -lh logs/
echo

echo "=== 최근 에러 로그 ==="
grep "ERROR" logs/notification-system.log | tail -10
```

### 4. 자주 묻는 질문 (FAQ)

#### Q: MongoDB 연결이 자주 끊어집니다.
**A**: 연결 풀 설정을 확인하고 네트워크 안정성을 점검하세요.

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/notificationdb?maxPoolSize=100&minPoolSize=5&maxIdleTimeMS=30000&waitQueueTimeoutMS=5000
```

#### Q: Kafka 메시지가 처리되지 않습니다.
**A**: Consumer Group 설정과 토픽 파티션 수를 확인하세요.

```properties
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.listener.concurrency=3
```

#### Q: 애플리케이션이 느려집니다.
**A**: 메모리 사용량과 GC 로그를 확인하고 인덱스를 최적화하세요.

```bash
# GC 로그 확인
jstat -gc <pid> 1000 10

# 인덱스 사용률 확인
db.users.aggregate([{ $indexStats: {} }])
```

#### Q: 대량 알림 전송 시 시스템이 멈춥니다.
**A**: 배치 크기를 조정하고 비동기 처리를 사용하세요.

```java
@Async
public CompletableFuture<Void> sendNotificationAsync(NotificationMessage message) {
    // 비동기 처리
    return CompletableFuture.completedFuture(null);
}
``` 