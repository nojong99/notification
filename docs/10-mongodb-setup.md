# MongoDB 설치 및 설정 가이드

## 1. MongoDB 설치

### Windows에서 MongoDB 설치

1. **MongoDB Community Server 다운로드**
   - [MongoDB 공식 사이트](https://www.mongodb.com/try/download/community)에서 다운로드
   - Windows x64 버전 선택
   - MSI 인스톨러 다운로드

2. **설치 과정**
   ```
   - MongoDB Compass 설치 옵션 체크 (GUI 도구)
   - Install MongoDB as a Service 체크
   - Data Directory: C:\data\db (기본값)
   - Log Directory: C:\data\log (기본값)
   ```

3. **환경 변수 설정**
   - 시스템 환경 변수에 MongoDB bin 폴더 추가
   - Path에 `C:\Program Files\MongoDB\Server\6.0\bin` 추가

### Docker를 사용한 MongoDB 설치 (권장)

1. **Docker Compose 파일 생성**
   ```yaml
   # docker-compose.yml
   version: '3.8'
   services:
     mongodb:
       image: mongo:6.0
       container_name: notification_mongodb
       restart: always
       ports:
         - "27017:27017"
       environment:
         MONGO_INITDB_ROOT_USERNAME: admin
         MONGO_INITDB_ROOT_PASSWORD: password
       volumes:
         - mongodb_data:/data/db
         - ./mongo-init.js:/docker-entrypoint-initdb.d/mongo-init.js:ro
   
   volumes:
     mongodb_data:
   ```

2. **MongoDB 초기화 스크립트 생성**
   ```javascript
   // mongo-init.js
   db = db.getSiblingDB('notification_system');
   
   // 사용자 생성
   db.createUser({
     user: 'notification_user',
     pwd: 'notification_pass',
     roles: [
       {
         role: 'readWrite',
         db: 'notification_system'
       }
     ]
   });
   
   // 컬렉션 생성
   db.createCollection('users');
   db.createCollection('notifications');
   
   // 인덱스 생성
   db.users.createIndex({ "username": 1 }, { unique: true });
   db.users.createIndex({ "email": 1 }, { unique: true });
   db.notifications.createIndex({ "userId": 1 });
   db.notifications.createIndex({ "status": 1 });
   ```

3. **Docker Compose 실행**
   ```bash
   docker-compose up -d
   ```

## 2. 애플리케이션 설정

### application.properties 설정

```properties
# MongoDB 설정
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=notification_system
spring.data.mongodb.auto-index-creation=true

# Docker를 사용한 경우 인증 설정
# spring.data.mongodb.username=notification_user
# spring.data.mongodb.password=notification_pass
# spring.data.mongodb.authentication-database=notification_system
```

## 3. MongoDB 연결 확인

### MongoDB Compass 사용
1. MongoDB Compass 실행
2. 연결 문자열 입력: `mongodb://localhost:27017`
3. notification_system 데이터베이스 확인

### 명령줄에서 확인
```bash
# MongoDB 쉘 접속
mongosh

# 데이터베이스 목록 확인
show dbs

# notification_system 데이터베이스 사용
use notification_system

# 컬렉션 목록 확인
show collections

# 사용자 데이터 확인
db.users.find()

# 알림 데이터 확인
db.notifications.find()
```

## 4. 애플리케이션 실행

```bash
# 프로젝트 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```

## 5. 데이터 영속성 확인

MongoDB를 사용하면 애플리케이션을 재시작해도 데이터가 유지됩니다:

1. 애플리케이션 실행
2. 로그인하여 사용자 생성
3. 알림 전송
4. 애플리케이션 종료
5. 애플리케이션 재시작
6. 데이터가 그대로 유지되는지 확인

## 6. 문제 해결

### MongoDB 연결 오류
- MongoDB 서비스가 실행 중인지 확인
- 포트 27017이 사용 가능한지 확인
- 방화벽 설정 확인

### 권한 오류
- MongoDB 사용자 생성 확인
- 인증 정보 설정 확인

### 데이터베이스 초기화
```bash
# MongoDB 쉘에서
use notification_system
db.dropDatabase()
```

## 7. 백업 및 복원

### 데이터 백업
```bash
mongodump --db notification_system --out ./backup
```

### 데이터 복원
```bash
mongorestore --db notification_system ./backup/notification_system
```

이제 MongoDB를 사용하여 데이터가 영속적으로 저장되며, 애플리케이션을 재시작해도 데이터가 유지됩니다! 