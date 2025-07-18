# 시작하기 (Getting Started)

이 가이드는 사용자 알림 시스템을 처음부터 설정하고 실행하는 방법을 설명합니다.

## 📋 목차

- [시스템 요구사항](#시스템-요구사항)
- [환경 설정](#환경-설정)
- [프로젝트 클론](#프로젝트-클론)
- [데이터베이스 설정](#데이터베이스-설정)
- [Kafka 설정](#kafka-설정)
- [애플리케이션 실행](#애플리케이션-실행)
- [첫 번째 테스트](#첫-번째-테스트)

## 🖥️ 시스템 요구사항

### 필수 소프트웨어

| 소프트웨어 | 최소 버전 | 권장 버전 |
|------------|-----------|-----------|
| Java | 17 | 17+ |
| MongoDB | 4.4 | 6.0+ |
| Apache Kafka | 2.8 | 3.6+ |
| Gradle | 7.0 | 8.0+ |

### 하드웨어 요구사항

- **CPU**: 2코어 이상
- **메모리**: 4GB 이상
- **디스크**: 10GB 이상의 여유 공간

## ⚙️ 환경 설정

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

# 3. MongoDB Shell 설치 (선택사항)
# https://www.mongodb.com/try/download/shell
```

#### macOS
```bash
# Homebrew 사용
brew tap mongodb/brew
brew install mongodb-community

# MongoDB 서비스 시작
brew services start mongodb/brew/mongodb-community
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
```

### 3. Apache Kafka 설치

#### 모든 플랫폼
```bash
# 1. Kafka 다운로드
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz

# 2. 압축 해제
tar -xzf kafka_2.13-3.6.0.tgz
cd kafka_2.13-3.6.0

# 3. Zookeeper 시작 (백그라운드)
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties

# 4. Kafka 시작 (백그라운드)
bin/kafka-server-start.sh -daemon config/server.properties
```

## 📥 프로젝트 클론

```bash
# 1. 프로젝트 클론
git clone https://github.com/nojong99/notification.git
cd notification

# 2. 프로젝트 구조 확인
ls -la
```

## 🗄️ 데이터베이스 설정

### MongoDB 연결 확인

```bash
# MongoDB 연결 테스트
mongosh
# 또는
mongo

# 데이터베이스 생성 (자동 생성됨)
use notificationdb

# 컬렉션 확인
show collections
```

### MongoDB 설정 파일

`src/main/resources/application.properties`에서 MongoDB 설정을 확인하세요:

```properties
# MongoDB 설정
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=notificationdb
```

## 📨 Kafka 설정

### Kafka 토픽 생성

```bash
# Kafka 디렉토리로 이동
cd kafka_2.13-3.6.0

# 알림 토픽 생성
bin/kafka-topics.sh --create \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# 토픽 목록 확인
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

### Kafka 설정 파일

`src/main/resources/application.properties`에서 Kafka 설정을 확인하세요:

```properties
# Kafka 설정
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.auto-offset-reset=earliest
```

## 🚀 애플리케이션 실행

### 1. 의존성 설치

```bash
# Gradle 래퍼 권한 설정 (Linux/macOS)
chmod +x gradlew

# 의존성 다운로드
./gradlew build
```

### 2. 애플리케이션 실행

```bash
# 개발 모드로 실행
./gradlew bootRun

# 또는 JAR 파일로 실행
./gradlew build
java -jar build/libs/play-0.0.1-SNAPSHOT.jar
```

### 3. 실행 확인

애플리케이션이 성공적으로 시작되면 다음 로그를 확인할 수 있습니다:

```
2024-01-01 12:00:00.000  INFO 1234 --- [main] c.e.d.PlayApplication : Started PlayApplication in 5.234 seconds
```

## 🧪 첫 번째 테스트

### 1. 사용자 생성

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "phoneNumber": "010-1234-5678"
  }'
```

**응답 예시:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "username": "testuser",
  "email": "test@example.com",
  "phoneNumber": "010-1234-5678",
  "active": true
}
```

### 2. 사용자 목록 조회

```bash
curl http://localhost:8080/api/users
```

### 3. 알림 전송

```bash
# 위에서 생성한 사용자 ID를 사용
curl -X POST "http://localhost:8080/api/notifications/send/user/507f1f77bcf86cd799439011?title=환영합니다&message=시스템에 오신 것을 환영합니다&type=EMAIL"
```

**응답 예시:**
```
알림이 큐에 추가되었습니다.
```

### 4. 알림 상태 확인

```bash
# 사용자별 알림 조회
curl http://localhost:8080/api/notifications/user/507f1f77bcf86cd799439011

# 상태별 알림 조회
curl http://localhost:8080/api/notifications/status/SENT
```

## 🔍 로그 확인

### 애플리케이션 로그

```bash
# 실시간 로그 확인
tail -f logs/application.log
```

### Kafka 로그

```bash
# Kafka 메시지 확인
cd kafka_2.13-3.6.0
bin/kafka-console-consumer.sh \
  --topic notifications \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

## 🎯 다음 단계

시스템이 정상적으로 실행되면 다음 단계로 진행하세요:

1. [API 문서](API-Documentation) - 전체 API 엔드포인트 확인
2. [아키텍처 개요](Architecture-Overview) - 시스템 구조 이해
3. [개발 가이드](Development-Guide) - 개발 환경 설정

## ❗ 문제 해결

문제가 발생하면 [트러블슈팅](Troubleshooting) 페이지를 참고하세요.

### 일반적인 문제

1. **포트 충돌**: 8080, 27017, 9092 포트가 사용 중인지 확인
2. **메모리 부족**: JVM 힙 메모리 설정 확인
3. **네트워크 연결**: 방화벽 설정 확인 