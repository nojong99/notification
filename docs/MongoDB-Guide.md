# MongoDB 가이드 (MongoDB Guide)

MongoDB를 사용한 데이터 저장 및 관리 방법에 대한 상세 가이드입니다.

## 📋 목차

- [MongoDB 개요](#mongodb-개요)
- [설치 및 설정](#설치-및-설정)
- [Spring Data MongoDB](#spring-data-mongodb)
- [데이터 모델링](#데이터-모델링)
- [쿼리 및 인덱싱](#쿼리-및-인덱싱)
- [성능 최적화](#성능-최적화)
- [모니터링](#모니터링)
- [트러블슈팅](#트러블슈팅)

## 🗄️ MongoDB 개요

### MongoDB란?

MongoDB는 문서 지향 NoSQL 데이터베이스로, JSON과 유사한 BSON(Binary JSON) 형식으로 데이터를 저장합니다.

### 주요 특징

- **문서 지향**: JSON과 유사한 문서 구조
- **스키마 유연성**: 동적 스키마 지원
- **수평 확장**: 샤딩을 통한 대용량 데이터 처리
- **고성능**: 인메모리 처리 및 인덱싱
- **복잡한 쿼리**: 중첩 문서, 배열 쿼리 지원

### 관계형 DB vs MongoDB

| 특징 | 관계형 DB | MongoDB |
|------|-----------|---------|
| 데이터 구조 | 테이블, 행, 열 | 컬렉션, 문서, 필드 |
| 스키마 | 고정 스키마 | 동적 스키마 |
| 관계 | 외래키, 조인 | 임베딩, 참조 |
| 확장성 | 수직 확장 | 수평 확장 |
| 쿼리 언어 | SQL | MongoDB Query Language |

## ⚙️ 설치 및 설정

### 1. MongoDB 설치

#### Windows
```bash
# 1. MongoDB Community Server 다운로드
# https://www.mongodb.com/try/download/community

# 2. 설치 후 서비스 시작
net start MongoDB

# 3. MongoDB Shell 설치
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

### 2. MongoDB 연결 확인

```bash
# MongoDB Shell 접속
mongosh
# 또는
mongo

# 데이터베이스 목록 확인
show dbs

# 데이터베이스 생성/전환
use notificationdb

# 컬렉션 목록 확인
show collections
```

### 3. Spring Boot 설정

#### application.properties
```properties
# MongoDB 연결 설정
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=notificationdb

# 연결 풀 설정
spring.data.mongodb.uri=mongodb://localhost:27017/notificationdb?maxPoolSize=100&minPoolSize=5

# 로깅 설정
logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG
```

#### build.gradle
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
}
```

## 🔧 Spring Data MongoDB

### 1. Document 매핑

#### 기본 어노테이션

```java
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    @Field("user_name")
    private String username;
    
    @Indexed(unique = true)
    private String email;
    
    private boolean active = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

#### 어노테이션 설명

| 어노테이션 | 설명 | 예시 |
|------------|------|------|
| `@Document` | MongoDB 컬렉션과 매핑 | `@Document(collection = "users")` |
| `@Id` | MongoDB의 `_id` 필드와 매핑 | `@Id private String id;` |
| `@Field` | 특정 필드명 매핑 | `@Field("user_name")` |
| `@Indexed` | 인덱스 생성 | `@Indexed(unique = true)` |
| `@CreatedDate` | 생성 시간 자동 설정 | `@CreatedDate` |
| `@LastModifiedDate` | 수정 시간 자동 설정 | `@LastModifiedDate` |

### 2. Repository 패턴

#### 기본 Repository

```java
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    // 기본 CRUD 메서드 자동 제공
    // save(), findById(), findAll(), deleteById() 등
}
```

#### 커스텀 쿼리 메서드

```java
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    // 단순 쿼리
    List<User> findByActiveTrue();
    User findByEmail(String email);
    
    // 복합 쿼리
    List<User> findByUsernameAndActive(String username, boolean active);
    List<User> findByEmailOrPhoneNumber(String email, String phoneNumber);
    
    // 정렬
    List<User> findByActiveTrueOrderByCreatedAtDesc();
    
    // 제한
    List<User> findTop10ByActiveTrue();
    
    // 존재 여부
    boolean existsByEmail(String email);
    
    // 개수
    long countByActiveTrue();
}
```

#### 쿼리 메서드 규칙

| 키워드 | 설명 | MongoDB 쿼리 |
|--------|------|--------------|
| `findBy` | 조회 | `db.users.find({field: value})` |
| `findBy...And` | AND 조건 | `db.users.find({field1: value1, field2: value2})` |
| `findBy...Or` | OR 조건 | `db.users.find({$or: [{field1: value1}, {field2: value2}]})` |
| `findBy...True` | boolean true | `db.users.find({field: true})` |
| `findBy...False` | boolean false | `db.users.find({field: false})` |
| `OrderBy` | 정렬 | `db.users.find().sort({field: 1})` |
| `Top` | 제한 | `db.users.find().limit(10)` |

### 3. MongoTemplate 사용

```java
@Service
public class UserService {
    private final MongoTemplate mongoTemplate;
    
    public UserService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    // 복잡한 쿼리
    public List<User> findUsersByComplexCriteria(String email, boolean active) {
        Criteria criteria = Criteria.where("email").regex(email, "i")
                                  .and("active").is(active);
        Query query = Query.query(criteria);
        return mongoTemplate.find(query, User.class);
    }
    
    // 집계 쿼리
    public long getActiveUserCount() {
        Criteria criteria = Criteria.where("active").is(true);
        Query query = Query.query(criteria);
        return mongoTemplate.count(query, User.class);
    }
}
```

## 📊 데이터 모델링

### 1. 문서 구조 설계

#### 사용자 문서
```json
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "username": "testuser",
  "email": "test@example.com",
  "phoneNumber": "010-1234-5678",
  "active": true,
  "profile": {
    "firstName": "홍",
    "lastName": "길동",
    "birthDate": "1990-01-01"
  },
  "preferences": {
    "emailNotification": true,
    "smsNotification": false,
    "pushNotification": true
  },
  "createdAt": ISODate("2024-01-01T12:00:00Z"),
  "updatedAt": ISODate("2024-01-01T12:00:00Z")
}
```

#### 알림 문서
```json
{
  "_id": ObjectId("507f1f77bcf86cd799439021"),
  "userId": "507f1f77bcf86cd799439011",
  "title": "환영합니다",
  "message": "시스템에 오신 것을 환영합니다",
  "type": "EMAIL",
  "status": "SENT",
  "metadata": {
    "templateId": "welcome_email",
    "priority": "normal",
    "retryCount": 0
  },
  "createdAt": ISODate("2024-01-01T12:00:00Z"),
  "sentAt": ISODate("2024-01-01T12:00:05Z")
}
```

### 2. 관계 설계

#### 임베딩 (Embedding)
```java
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    
    // 임베딩된 문서
    private Profile profile;
    private List<Address> addresses;
}

public class Profile {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String avatar;
}
```

#### 참조 (Reference)
```java
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    
    // 참조 (사용자 ID)
    private String userId;
    
    private String title;
    private String message;
    private String type;
    private String status;
}
```

### 3. 인덱스 설계

```java
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    @Indexed
    private String username;
    
    @Indexed
    private boolean active;
    
    @Indexed
    private LocalDateTime createdAt;
}

// 복합 인덱스
@CompoundIndex(name = "email_active_idx", def = "{'email': 1, 'active': 1}")
@CompoundIndex(name = "created_at_idx", def = "{'createdAt': -1}")
public class User {
    // ...
}
```

## 🔍 쿼리 및 인덱싱

### 1. 기본 쿼리

#### MongoDB Shell
```javascript
// 모든 사용자 조회
db.users.find()

// 활성 사용자만 조회
db.users.find({active: true})

// 이메일로 사용자 조회
db.users.findOne({email: "test@example.com"})

// 정렬
db.users.find().sort({createdAt: -1})

// 제한
db.users.find().limit(10)

// 건너뛰기
db.users.find().skip(20).limit(10)
```

#### Spring Data MongoDB
```java
// 기본 조회
List<User> allUsers = userRepository.findAll();

// 조건 조회
List<User> activeUsers = userRepository.findByActiveTrue();

// 단일 조회
User user = userRepository.findByEmail("test@example.com");

// 정렬
List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

// 페이징
Page<User> userPage = userRepository.findAll(PageRequest.of(0, 10));
```

### 2. 고급 쿼리

#### 텍스트 검색
```java
// 텍스트 인덱스 생성
@TextIndexed
private String username;

@TextIndexed
private String email;

// 텍스트 검색
public List<User> searchUsers(String searchTerm) {
    TextCriteria criteria = TextCriteria.forDefaultLanguage()
        .matchingAny(searchTerm);
    Query query = TextQuery.queryText(criteria);
    return mongoTemplate.find(query, User.class);
}
```

#### 집계 쿼리
```java
public Map<String, Long> getUserStats() {
    Aggregation aggregation = Aggregation.newAggregation(
        Aggregation.group("active")
            .count().as("count")
    );
    
    AggregationResults<Document> results = 
        mongoTemplate.aggregate(aggregation, "users", Document.class);
    
    Map<String, Long> stats = new HashMap<>();
    for (Document doc : results) {
        stats.put(doc.getString("_id"), doc.getLong("count"));
    }
    return stats;
}
```

### 3. 인덱스 관리

#### 인덱스 생성
```javascript
// 단일 필드 인덱스
db.users.createIndex({email: 1})

// 고유 인덱스
db.users.createIndex({email: 1}, {unique: true})

// 복합 인덱스
db.users.createIndex({active: 1, createdAt: -1})

// 텍스트 인덱스
db.users.createIndex({username: "text", email: "text"})

// TTL 인덱스 (자동 삭제)
db.notifications.createIndex({createdAt: 1}, {expireAfterSeconds: 7776000}) // 90일
```

#### 인덱스 확인
```javascript
// 인덱스 목록
db.users.getIndexes()

// 인덱스 사용 통계
db.users.aggregate([
    { $indexStats: {} }
])
```

## ⚡ 성능 최적화

### 1. 쿼리 최적화

#### 프로젝션 사용
```java
// 필요한 필드만 조회
public List<UserSummary> getUserSummaries() {
    Query query = new Query();
    query.fields().include("username").include("email").include("active");
    return mongoTemplate.find(query, UserSummary.class, "users");
}
```

#### 배치 처리
```java
// 대량 데이터 처리
public void processUsersInBatches() {
    int batchSize = 1000;
    int skip = 0;
    
    while (true) {
        Page<User> userPage = userRepository.findAll(
            PageRequest.of(skip / batchSize, batchSize)
        );
        
        if (userPage.isEmpty()) break;
        
        // 배치 처리 로직
        processUsers(userPage.getContent());
        
        skip += batchSize;
    }
}
```

### 2. 인덱스 최적화

#### 인덱스 선택
```javascript
// 쿼리 실행 계획 확인
db.users.find({active: true, email: "test@example.com"}).explain("executionStats")

// 인덱스 힌트
db.users.find({active: true}).hint({active: 1, email: 1})
```

#### 인덱스 크기 최적화
```javascript
// 부분 인덱스 (조건부 인덱스)
db.users.createIndex(
    {email: 1}, 
    {partialFilterExpression: {active: true}}
)

// 스파스 인덱스 (null 값 제외)
db.users.createIndex(
    {phoneNumber: 1}, 
    {sparse: true}
)
```

### 3. 연결 풀 최적화

```properties
# application.properties
spring.data.mongodb.uri=mongodb://localhost:27017/notificationdb?maxPoolSize=100&minPoolSize=5&maxIdleTimeMS=30000&waitQueueTimeoutMS=5000
```

## 📊 모니터링

### 1. MongoDB 모니터링

#### 서버 상태 확인
```javascript
// 서버 상태
db.serverStatus()

// 데이터베이스 통계
db.stats()

// 컬렉션 통계
db.users.stats()
```

#### 성능 모니터링
```javascript
// 현재 실행 중인 쿼리
db.currentOp()

// 느린 쿼리 로그
db.getProfilingStatus()
db.setProfilingLevel(1, 100) // 100ms 이상 쿼리 로깅
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
```

#### 커스텀 메트릭
```java
@Component
public class MongoMetrics {
    private final MeterRegistry meterRegistry;
    private final MongoTemplate mongoTemplate;
    
    public MongoMetrics(MeterRegistry meterRegistry, MongoTemplate mongoTemplate) {
        this.meterRegistry = meterRegistry;
        this.mongoTemplate = mongoTemplate;
    }
    
    @Scheduled(fixedRate = 60000) // 1분마다
    public void recordUserCount() {
        long userCount = mongoTemplate.count(new Query(), User.class);
        meterRegistry.gauge("mongodb.users.count", userCount);
    }
}
```

## 🔧 트러블슈팅

### 1. 일반적인 문제

#### 연결 문제
```bash
# MongoDB 서비스 상태 확인
sudo systemctl status mongod

# 포트 확인
netstat -tlnp | grep 27017

# 로그 확인
tail -f /var/log/mongodb/mongod.log
```

#### 성능 문제
```javascript
// 느린 쿼리 확인
db.system.profile.find().sort({ts: -1}).limit(10)

// 인덱스 사용률 확인
db.users.aggregate([
    { $indexStats: {} }
])
```

### 2. 디버깅

#### Spring Boot 로깅
```properties
# application.properties
logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG
logging.level.org.springframework.data.mongodb.core.query.Query=DEBUG
```

#### MongoDB 로깅
```javascript
// 쿼리 로깅 활성화
db.setProfilingLevel(2) // 모든 쿼리 로깅

// 로깅 비활성화
db.setProfilingLevel(0)
```

### 3. 백업 및 복구

```bash
# 데이터베이스 백업
mongodump --db notificationdb --out /backup

# 데이터베이스 복구
mongorestore --db notificationdb /backup/notificationdb

# 컬렉션 백업
mongoexport --db notificationdb --collection users --out users.json

# 컬렉션 복구
mongoimport --db notificationdb --collection users --file users.json
```

## 📚 추가 리소스

- [MongoDB 공식 문서](https://docs.mongodb.com/)
- [Spring Data MongoDB 문서](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [MongoDB University](https://university.mongodb.com/) - 무료 온라인 강의
- [MongoDB Atlas](https://www.mongodb.com/atlas) - 클라우드 MongoDB 서비스 