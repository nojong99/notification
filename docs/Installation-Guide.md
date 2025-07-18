# 설치 가이드 (Installation Guide)

사용자 알림 시스템의 상세한 설치 및 배포 가이드입니다.

## 📋 목차

- [시스템 요구사항](#시스템-요구사항)
- [개발 환경 설정](#개발-환경-설정)
- [프로덕션 환경 설정](#프로덕션-환경-설정)
- [Docker 배포](#docker-배포)
- [클라우드 배포](#클라우드-배포)
- [모니터링 설정](#모니터링-설정)

## 🖥️ 시스템 요구사항

### 최소 요구사항

| 구성요소 | 최소 사양 | 권장 사양 |
|----------|-----------|-----------|
| **CPU** | 2코어 | 4코어 이상 |
| **메모리** | 4GB RAM | 8GB RAM 이상 |
| **디스크** | 20GB 여유 공간 | 50GB 이상 |
| **네트워크** | 100Mbps | 1Gbps 이상 |
| **OS** | Linux, Windows, macOS | Linux (Ubuntu 20.04+) |

### 소프트웨어 요구사항

| 소프트웨어 | 최소 버전 | 권장 버전 |
|------------|-----------|-----------|
| **Java** | 17 | 17+ |
| **MongoDB** | 4.4 | 6.0+ |
| **Apache Kafka** | 2.8 | 3.6+ |
| **Gradle** | 7.0 | 8.0+ |
| **Docker** | 20.10 | 24.0+ |

## ⚙️ 개발 환경 설정

### 1. Java 설치

#### Windows
```bash
# 1. OpenJDK 17 다운로드
# https://adoptium.net/temurin/releases/?version=17

# 2. 환경 변수 설정
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

# 3. 설치 확인
java -version
javac -version
```

#### macOS
```bash
# Homebrew 사용
brew install openjdk@17

# 환경 변수 설정
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@17' >> ~/.zshrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc
source ~/.zshrc

# 설치 확인
java -version
```

#### Linux (Ubuntu/Debian)
```bash
# 패키지 업데이트
sudo apt update

# OpenJDK 17 설치
sudo apt install openjdk-17-jdk

# 환경 변수 설정
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# 설치 확인
java -version
```

### 2. MongoDB 설치

#### Windows
```bash
# 1. MongoDB Community Server 다운로드
# https://www.mongodb.com/try/download/community

# 2. 설치 후 서비스 시작
net start MongoDB

# 3. MongoDB Shell 설치
# https://www.mongodb.com/try/download/shell

# 4. 연결 테스트
mongosh
```

#### macOS
```bash
# Homebrew 사용
brew tap mongodb/brew
brew install mongodb-community

# MongoDB 서비스 시작
brew services start mongodb/brew/mongodb-community

# 연결 테스트
mongosh
```

#### Linux (Ubuntu/Debian)
```bash
# MongoDB GPG 키 추가
wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | sudo apt-key add -

# MongoDB 저장소 추가
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/6.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-6.0.list

# MongoDB 설치
sudo apt update
sudo apt install mongodb-org

# MongoDB 서비스 시작
sudo systemctl start mongod
sudo systemctl enable mongod

# 연결 테스트
mongosh
```

### 3. Apache Kafka 설치

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

# 4. Zookeeper 시작
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties

# 5. Kafka 시작
bin/kafka-server-start.sh -daemon config/server.properties

# 6. 토픽 생성
bin/kafka-topics.sh --create \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

### 4. 프로젝트 설정

```bash
# 1. 프로젝트 클론
git clone https://github.com/nojong99/notification.git
cd notification

# 2. 의존성 다운로드
./gradlew build

# 3. 애플리케이션 실행
./gradlew bootRun
```

## 🚀 프로덕션 환경 설정

### 1. 시스템 최적화

#### Linux 시스템 최적화
```bash
# 파일 디스크립터 제한 증가
echo "* soft nofile 65536" >> /etc/security/limits.conf
echo "* hard nofile 65536" >> /etc/security/limits.conf

# 스왑 메모리 설정
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 네트워크 최적화
echo 'net.core.somaxconn = 65535' >> /etc/sysctl.conf
echo 'net.ipv4.tcp_max_syn_backlog = 65535' >> /etc/sysctl.conf
sudo sysctl -p
```

#### JVM 최적화
```bash
# JVM 힙 메모리 설정
export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 애플리케이션 실행
java $JAVA_OPTS -jar notification-system.jar
```

### 2. MongoDB 프로덕션 설정

#### MongoDB 설정 파일
```yaml
# /etc/mongod.conf
systemLog:
  destination: file
  logAppend: true
  path: /var/log/mongodb/mongod.log

storage:
  dbPath: /var/lib/mongodb
  journal:
    enabled: true

net:
  port: 27017
  bindIp: 0.0.0.0

security:
  authorization: enabled

operationProfiling:
  slowOpThresholdMs: 100
  mode: slowOp

setParameter:
  enableLocalhostAuthBypass: false
```

#### MongoDB 인덱스 생성
```javascript
// MongoDB Shell에서 실행
use notificationdb

// 사용자 컬렉션 인덱스
db.users.createIndex({email: 1}, {unique: true})
db.users.createIndex({active: 1})
db.users.createIndex({createdAt: -1})

// 알림 컬렉션 인덱스
db.notifications.createIndex({userId: 1})
db.notifications.createIndex({status: 1})
db.notifications.createIndex({createdAt: -1})
db.notifications.createIndex({createdAt: 1}, {expireAfterSeconds: 7776000}) // 90일 TTL
```

### 3. Kafka 프로덕션 설정

#### Kafka 설정 파일
```properties
# config/server.properties
broker.id=0
listeners=PLAINTEXT://:9092
log.dirs=/var/lib/kafka-logs
num.partitions=3
default.replication.factor=1
min.insync.replicas=1
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
zookeeper.connect=localhost:2181
zookeeper.connection.timeout.ms=18000
group.initial.rebalance.delay.ms=0
```

#### Kafka 토픽 설정
```bash
# 토픽 생성 (프로덕션용)
bin/kafka-topics.sh --create \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --partitions 6 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config segment.bytes=1073741824 \
  --config cleanup.policy=delete

# 토픽 설정 확인
bin/kafka-topics.sh --describe \
  --topic notifications \
  --bootstrap-server localhost:9092
```

### 4. 애플리케이션 프로덕션 설정

#### application-prod.properties
```properties
# 서버 설정
server.port=8080
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10

# MongoDB 설정
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=notificationdb
spring.data.mongodb.uri=mongodb://localhost:27017/notificationdb?maxPoolSize=100&minPoolSize=5&maxIdleTimeMS=30000&waitQueueTimeoutMS=5000

# Kafka 설정
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.batch-size=32768
spring.kafka.producer.buffer-memory=67108864
spring.kafka.producer.compression-type=snappy

# 로깅 설정
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.kafka=INFO
logging.file.name=logs/notification-system.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Actuator 설정
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
```

#### 시스템 서비스 설정 (systemd)
```ini
# /etc/systemd/system/notification-system.service
[Unit]
Description=Notification System
After=network.target mongod.service kafka.service

[Service]
Type=simple
User=notification
WorkingDirectory=/opt/notification-system
ExecStart=/usr/bin/java -Xms2g -Xmx4g -XX:+UseG1GC -jar notification-system.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 서비스 등록 및 시작
sudo systemctl daemon-reload
sudo systemctl enable notification-system
sudo systemctl start notification-system
sudo systemctl status notification-system
```

## 🐳 Docker 배포

### 1. Docker Compose 설정

#### docker-compose.yml
```yaml
version: '3.8'

services:
  mongodb:
    image: mongo:6.0
    container_name: notification-mongodb
    restart: always
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: password
      MONGO_INITDB_DATABASE: notificationdb
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
      - ./mongo-init.js:/docker-entrypoint-initdb.d/mongo-init.js:ro
    networks:
      - notification-network

  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: notification-zookeeper
    restart: always
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    networks:
      - notification-network

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: notification-kafka
    restart: always
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: false
    ports:
      - "9092:9092"
    volumes:
      - kafka_data:/var/lib/kafka/data
    networks:
      - notification-network

  notification-app:
    build: .
    container_name: notification-app
    restart: always
    depends_on:
      - mongodb
      - kafka
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATA_MONGODB_URI: mongodb://admin:password@mongodb:27017/notificationdb?authSource=admin
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8080:8080"
    volumes:
      - app_logs:/app/logs
    networks:
      - notification-network

volumes:
  mongodb_data:
  kafka_data:
  app_logs:

networks:
  notification-network:
    driver: bridge
```

#### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Gradle 래퍼 및 소스 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# 권한 설정
RUN chmod +x gradlew

# 애플리케이션 빌드
RUN ./gradlew build -x test

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 로그 디렉토리 생성
RUN mkdir -p logs

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-Xms1g", "-Xmx2g", "-XX:+UseG1GC", "-jar", "app.jar"]
```

#### application-docker.properties
```properties
# Docker 환경 설정
spring.data.mongodb.uri=mongodb://admin:password@mongodb:27017/notificationdb?authSource=admin
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.auto-offset-reset=earliest

# 로깅 설정
logging.file.name=logs/notification-system.log
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
```

### 2. Docker 배포 실행

```bash
# 1. Docker Compose 실행
docker-compose up -d

# 2. 서비스 상태 확인
docker-compose ps

# 3. 로그 확인
docker-compose logs -f notification-app

# 4. 토픽 생성
docker exec notification-kafka kafka-topics --create \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# 5. 서비스 중지
docker-compose down
```

## ☁️ 클라우드 배포

### 1. AWS 배포

#### EC2 인스턴스 설정
```bash
# Ubuntu 20.04 LTS 인스턴스 생성
# 인스턴스 타입: t3.medium 이상
# 스토리지: 20GB 이상

# 보안 그룹 설정
# - SSH (22)
# - HTTP (80)
# - HTTPS (443)
# - MongoDB (27017)
# - Kafka (9092)
# - 애플리케이션 (8080)
```

#### AWS 배포 스크립트
```bash
#!/bin/bash
# deploy-aws.sh

# 시스템 업데이트
sudo apt update && sudo apt upgrade -y

# Java 설치
sudo apt install openjdk-17-jdk -y

# MongoDB 설치
wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | sudo apt-key add -
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/6.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-6.0.list
sudo apt update
sudo apt install mongodb-org -y
sudo systemctl start mongod
sudo systemctl enable mongod

# Kafka 설치
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz
tar -xzf kafka_2.13-3.6.0.tgz
sudo mv kafka_2.13-3.6.0 /opt/kafka

# Kafka 서비스 설정
sudo tee /etc/systemd/system/kafka.service > /dev/null <<EOF
[Unit]
Description=Apache Kafka
After=network.target

[Service]
Type=simple
User=kafka
Group=kafka
ExecStart=/opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# Kafka 사용자 생성
sudo useradd -r -s /bin/false kafka
sudo chown -R kafka:kafka /opt/kafka

# Kafka 시작
sudo systemctl enable kafka
sudo systemctl start kafka

# 애플리케이션 배포
sudo mkdir -p /opt/notification-system
sudo chown ubuntu:ubuntu /opt/notification-system
cd /opt/notification-system

# 애플리케이션 다운로드 및 실행
# (Git에서 클론하거나 JAR 파일 업로드)
```

### 2. Kubernetes 배포

#### Kubernetes 매니페스트
```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: notification-system
```

```yaml
# k8s/mongodb.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongodb
  namespace: notification-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongodb
  template:
    metadata:
      labels:
        app: mongodb
    spec:
      containers:
      - name: mongodb
        image: mongo:6.0
        ports:
        - containerPort: 27017
        env:
        - name: MONGO_INITDB_ROOT_USERNAME
          value: admin
        - name: MONGO_INITDB_ROOT_PASSWORD
          value: password
        volumeMounts:
        - name: mongodb-data
          mountPath: /data/db
      volumes:
      - name: mongodb-data
        persistentVolumeClaim:
          claimName: mongodb-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mongodb-service
  namespace: notification-system
spec:
  selector:
    app: mongodb
  ports:
  - port: 27017
    targetPort: 27017
  type: ClusterIP
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mongodb-pvc
  namespace: notification-system
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
```

```yaml
# k8s/kafka.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka
  namespace: notification-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
      - name: kafka
        image: confluentinc/cp-kafka:7.4.0
        ports:
        - containerPort: 9092
        env:
        - name: KAFKA_BROKER_ID
          value: "1"
        - name: KAFKA_ZOOKEEPER_CONNECT
          value: "zookeeper:2181"
        - name: KAFKA_ADVERTISED_LISTENERS
          value: "PLAINTEXT://kafka:9092"
        - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
          value: "1"
---
apiVersion: v1
kind: Service
metadata:
  name: kafka-service
  namespace: notification-system
spec:
  selector:
    app: kafka
  ports:
  - port: 9092
    targetPort: 9092
  type: ClusterIP
```

```yaml
# k8s/app.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notification-app
  namespace: notification-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: notification-app
  template:
    metadata:
      labels:
        app: notification-app
    spec:
      containers:
      - name: notification-app
        image: notification-system:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: SPRING_DATA_MONGODB_URI
          value: "mongodb://admin:password@mongodb-service:27017/notificationdb?authSource=admin"
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: notification-app-service
  namespace: notification-system
spec:
  selector:
    app: notification-app
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

#### Kubernetes 배포 실행
```bash
# 네임스페이스 생성
kubectl apply -f k8s/namespace.yaml

# MongoDB 배포
kubectl apply -f k8s/mongodb.yaml

# Kafka 배포
kubectl apply -f k8s/kafka.yaml

# 애플리케이션 배포
kubectl apply -f k8s/app.yaml

# 배포 상태 확인
kubectl get all -n notification-system

# 로그 확인
kubectl logs -f deployment/notification-app -n notification-system
```

## 📊 모니터링 설정

### 1. Prometheus 설정

#### prometheus.yml
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'notification-system'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'mongodb'
    static_configs:
      - targets: ['localhost:27017']

  - job_name: 'kafka'
    static_configs:
      - targets: ['localhost:9092']
```

### 2. Grafana 대시보드

#### 대시보드 설정
```json
{
  "dashboard": {
    "title": "Notification System Dashboard",
    "panels": [
      {
        "title": "HTTP Requests",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count[5m])",
            "legendFormat": "{{method}} {{uri}}"
          }
        ]
      },
      {
        "title": "Kafka Messages",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(kafka_producer_record_send_total[5m])",
            "legendFormat": "Producer"
          },
          {
            "expr": "rate(kafka_consumer_records_consumed_total[5m])",
            "legendFormat": "Consumer"
          }
        ]
      },
      {
        "title": "MongoDB Operations",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(mongodb_operations_total[5m])",
            "legendFormat": "{{operation}}"
          }
        ]
      }
    ]
  }
}
```

### 3. 로그 집계 (ELK Stack)

#### Logstash 설정
```conf
# logstash.conf
input {
  file {
    path => "/opt/notification-system/logs/*.log"
    type => "notification-system"
  }
}

filter {
  if [type] == "notification-system" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} %{DATA:logger} - %{GREEDYDATA:message}" }
    }
    date {
      match => [ "timestamp", "yyyy-MM-dd HH:mm:ss" ]
    }
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "notification-system-%{+YYYY.MM.dd}"
  }
}
```

## 🔧 배포 후 검증

### 1. 시스템 상태 확인

```bash
# 애플리케이션 상태 확인
curl http://localhost:8080/actuator/health

# MongoDB 연결 확인
mongosh --eval "db.runCommand('ping')"

# Kafka 토픽 확인
bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# Consumer Group 상태 확인
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group notification-group
```

### 2. 성능 테스트

```bash
# 사용자 생성 테스트
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","phoneNumber":"010-1234-5678"}'

# 알림 전송 테스트
curl -X POST "http://localhost:8080/api/notifications/send/user/{userId}?title=테스트&message=성능테스트&type=EMAIL"

# 대량 알림 전송 테스트
curl -X POST "http://localhost:8080/api/notifications/send/all?title=대량테스트&message=대량알림테스트&type=SMS"
```

### 3. 모니터링 확인

```bash
# 메트릭 확인
curl http://localhost:8080/actuator/metrics

# 로그 확인
tail -f logs/notification-system.log

# 시스템 리소스 확인
htop
df -h
free -h
``` 