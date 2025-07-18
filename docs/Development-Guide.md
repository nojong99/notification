# 개발 가이드 (Development Guide)

사용자 알림 시스템의 개발 환경 설정 및 개발 가이드입니다.

## 📋 목차

- [개발 환경 설정](#개발-환경-설정)
- [프로젝트 구조](#프로젝트-구조)
- [코딩 컨벤션](#코딩-컨벤션)
- [테스트 가이드](#테스트-가이드)
- [디버깅](#디버깅)
- [성능 최적화](#성능-최적화)

## ⚙️ 개발 환경 설정

### 1. IDE 설정

#### IntelliJ IDEA
```xml
<!-- .idea/codeStyles/Project.xml -->
<component name="ProjectCodeStyleConfiguration">
  <code_scheme name="Project" version="173">
    <JavaCodeStyleSettings>
      <option name="IMPORT_LAYOUT_TABLE">
        <value>
          <package name="java" withSubpackages="true" static="false" />
          <emptyLine />
          <package name="org" withSubpackages="true" static="false" />
          <emptyLine />
          <package name="com" withSubpackages="true" static="false" />
          <emptyLine />
          <package name="" withSubpackages="true" static="false" />
        </value>
      </option>
    </JavaCodeStyleSettings>
  </code_scheme>
</component>
```

#### Eclipse
```xml
<!-- .settings/org.eclipse.jdt.ui.prefs -->
eclipse.preferences.version=1
formatter_profile=_notification_system
formatter_settings_version=12
org.eclipse.jdt.ui.formatterprofiles=_notification_system=<?xml version\="1.0" encoding\="UTF-8"?><profiles version\="12"><profile name\="_notification_system" version\="12"><setting id\="org.eclipse.jdt.ui.formatter.lineSplit" value\="120"/><setting id\="org.eclipse.jdt.ui.formatter.tabulation.char" value\="space"/><setting id\="org.eclipse.jdt.ui.formatter.tabulation.size" value\="2"/></profile></profiles>
```

### 2. Git 설정

#### .gitignore
```gitignore
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/

# IntelliJ IDEA
.idea/
*.iws
*.iml
*.ipr
out/
!**/src/main/**/out/
!**/src/test/**/out/

# Eclipse
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache
bin/
!**/src/main/**/bin/
!**/src/test/**/bin/

# NetBeans
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/

# VS Code
.vscode/

# Mac
.DS_Store

# Windows
Thumbs.db
ehthumbs.db
Desktop.ini

# Logs
*.log
logs/

# Runtime data
pids
*.pid
*.seed
*.pid.lock

# Coverage directory used by tools like istanbul
coverage/

# Dependency directories
node_modules/

# Optional npm cache directory
.npm

# Optional REPL history
.node_repl_history

# Output of 'npm pack'
*.tgz

# Yarn Integrity file
.yarn-integrity

# dotenv environment variables file
.env

# Application specific
application-local.properties
application-dev.properties
```

#### Git Hooks
```bash
# .git/hooks/pre-commit
#!/bin/sh

# Gradle 빌드 테스트
./gradlew build -x test

# 코드 스타일 검사
./gradlew spotlessCheck

# 테스트 실행
./gradlew test

# 성공 시에만 커밋 허용
if [ $? -eq 0 ]; then
    echo "Pre-commit checks passed"
    exit 0
else
    echo "Pre-commit checks failed"
    exit 1
fi
```

### 3. 개발 도구 설정

#### Gradle 설정
```gradle
// build.gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.3'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'com.diffplug.spotless' version '6.25.0'
    id 'org.sonarqube' version '4.4.1.3373'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.kafka:spring-kafka'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    compileOnly 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
    annotationProcessor 'org.projectlombok:lombok'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
    testImplementation 'org.testcontainers:mongodb'
    testImplementation 'org.testcontainers:kafka'
    testImplementation 'org.testcontainers:junit-jupiter'
    
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
}

// Spotless 설정
spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// SonarQube 설정
sonarqube {
    properties {
        property "sonar.projectKey", "notification-system"
        property "sonar.projectName", "Notification System"
        property "sonar.host.url", "http://localhost:9000"
    }
}
```

## 📁 프로젝트 구조

### 1. 패키지 구조

```
src/main/java/com/example/demo/
├── PlayApplication.java              # 메인 애플리케이션
├── config/                           # 설정 클래스
│   ├── KafkaConfig.java             # Kafka 설정
│   ├── MongoConfig.java             # MongoDB 설정
│   └── WebConfig.java               # 웹 설정
├── controller/                       # REST API 컨트롤러
│   ├── UserController.java          # 사용자 API
│   ├── NotificationController.java  # 알림 API
│   └── HealthController.java        # 헬스체크 API
├── service/                         # 비즈니스 로직
│   ├── UserService.java            # 사용자 서비스
│   ├── NotificationService.java    # 알림 서비스
│   └── ValidationService.java      # 검증 서비스
├── repository/                      # 데이터 접근 계층
│   ├── UserRepository.java         # 사용자 Repository
│   └── NotificationRepository.java # 알림 Repository
├── model/                          # 도메인 모델
│   ├── User.java                   # 사용자 모델
│   ├── Notification.java           # 알림 모델
│   └── enums/                      # 열거형
│       ├── NotificationType.java   # 알림 타입
│       └── NotificationStatus.java # 알림 상태
├── dto/                            # 데이터 전송 객체
│   ├── UserDto.java                # 사용자 DTO
│   ├── NotificationDto.java        # 알림 DTO
│   └── NotificationMessage.java    # Kafka 메시지 DTO
├── kafka/                          # Kafka 관련
│   ├── NotificationProducer.java   # Kafka Producer
│   └── NotificationConsumer.java   # Kafka Consumer
├── exception/                      # 예외 처리
│   ├── GlobalExceptionHandler.java # 전역 예외 처리
│   ├── UserNotFoundException.java  # 사용자 없음 예외
│   └── NotificationException.java  # 알림 예외
└── util/                           # 유틸리티
    ├── DateUtils.java              # 날짜 유틸리티
    └── ValidationUtils.java        # 검증 유틸리티
```

### 2. 리소스 구조

```
src/main/resources/
├── application.properties           # 기본 설정
├── application-dev.properties      # 개발 환경 설정
├── application-prod.properties     # 프로덕션 환경 설정
├── application-test.properties     # 테스트 환경 설정
├── static/                         # 정적 리소스
│   ├── css/
│   ├── js/
│   └── images/
└── templates/                      # 템플릿 파일
    └── email/
        ├── welcome.html
        └── notification.html
```

### 3. 테스트 구조

```
src/test/java/com/example/demo/
├── PlayApplicationTests.java        # 통합 테스트
├── controller/                      # 컨트롤러 테스트
│   ├── UserControllerTest.java
│   └── NotificationControllerTest.java
├── service/                         # 서비스 테스트
│   ├── UserServiceTest.java
│   └── NotificationServiceTest.java
├── repository/                      # Repository 테스트
│   ├── UserRepositoryTest.java
│   └── NotificationRepositoryTest.java
├── kafka/                           # Kafka 테스트
│   ├── NotificationProducerTest.java
│   └── NotificationConsumerTest.java
└── integration/                     # 통합 테스트
    ├── NotificationFlowTest.java
    └── UserManagementTest.java
```

## 📝 코딩 컨벤션

### 1. Java 코딩 컨벤션

#### 클래스 명명 규칙
```java
// 클래스명: PascalCase
public class UserService {
    // 상수: UPPER_SNAKE_CASE
    private static final String DEFAULT_EMAIL_DOMAIN = "example.com";
    
    // 필드명: camelCase
    private final UserRepository userRepository;
    
    // 메서드명: camelCase
    public User createUser(UserDto userDto) {
        // 변수명: camelCase
        String email = userDto.getEmail();
        
        // 로컬 상수: camelCase
        final int maxRetryCount = 3;
        
        return userRepository.save(user);
    }
}
```

#### 어노테이션 순서
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    @GetMapping("/{id}")
    @Operation(summary = "사용자 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        // 구현
    }
}
```

#### 예외 처리
```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    public User getUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + id));
    }
    
    public void createUser(UserDto userDto) {
        try {
            validateUser(userDto);
            User user = userRepository.save(convertToUser(userDto));
            log.info("사용자 생성 완료: {}", user.getId());
        } catch (Exception e) {
            log.error("사용자 생성 실패: {}", e.getMessage(), e);
            throw new UserCreationException("사용자 생성에 실패했습니다", e);
        }
    }
}
```

### 2. Spring Boot 컨벤션

#### 컨트롤러 패턴
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @Operation(summary = "사용자 생성")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        log.info("사용자 생성 요청: {}", request.getEmail());
        
        User user = userService.createUser(request);
        UserResponse response = UserResponse.from(user);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "사용자 조회")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        User user = userService.getUserById(id);
        UserResponse response = UserResponse.from(user);
        
        return ResponseEntity.ok(response);
    }
}
```

#### 서비스 패턴
```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final ValidationService validationService;
    
    @Transactional
    public User createUser(UserRequest request) {
        log.info("사용자 생성 시작: {}", request.getEmail());
        
        // 검증
        validationService.validateUserRequest(request);
        
        // 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        
        // 변환 및 저장
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .active(true)
            .build();
        
        User savedUser = userRepository.save(user);
        
        log.info("사용자 생성 완료: {}", savedUser.getId());
        return savedUser;
    }
}
```

### 3. MongoDB 컨벤션

#### Document 클래스
```java
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    @Indexed
    private String username;
    
    private String phoneNumber;
    
    @Indexed
    private boolean active;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
}
```

#### Repository 인터페이스
```java
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    // 기본 쿼리 메서드
    Optional<User> findByEmail(String email);
    List<User> findByActiveTrue();
    boolean existsByEmail(String email);
    long countByActiveTrue();
    
    // 복합 쿼리 메서드
    List<User> findByUsernameAndActive(String username, boolean active);
    List<User> findByEmailOrPhoneNumber(String email, String phoneNumber);
    
    // 정렬 및 페이징
    Page<User> findByActiveTrue(Pageable pageable);
    List<User> findByActiveTrueOrderByCreatedAtDesc();
    
    // 커스텀 쿼리
    @Query("{'email': {$regex: ?0, $options: 'i'}}")
    List<User> findByEmailContainingIgnoreCase(String email);
    
    @Query("{'createdAt': {$gte: ?0, $lte: ?1}}")
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
```

### 4. Kafka 컨벤션

#### Producer 클래스
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String TOPIC = "notifications";
    
    public void sendNotification(NotificationMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            String key = message.getUserId();
            
            log.debug("Kafka 메시지 전송: topic={}, key={}", TOPIC, key);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(TOPIC, key, messageJson);
            
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
            throw new NotificationException("메시지 직렬화 실패", e);
        }
    }
}
```

#### Consumer 클래스
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    
    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void consumeNotification(String message) {
        try {
            log.debug("Kafka 메시지 수신: {}", message);
            
            NotificationMessage notificationMessage = 
                objectMapper.readValue(message, NotificationMessage.class);
            
            processNotification(notificationMessage);
            
        } catch (Exception e) {
            log.error("메시지 처리 오류: {}", e.getMessage(), e);
            // 실패한 메시지를 다른 토픽으로 전송하거나 재시도 로직 구현
        }
    }
    
    private void processNotification(NotificationMessage message) {
        try {
            log.info("알림 처리 시작: userId={}, type={}", 
                    message.getUserId(), message.getType());
            
            // 실제 알림 전송 로직
            notificationService.sendNotification(message);
            
            // 상태 업데이트
            updateNotificationStatus(message, "SENT");
            
            log.info("알림 처리 완료: userId={}", message.getUserId());
            
        } catch (Exception e) {
            log.error("알림 처리 실패: userId={}, error={}", 
                    message.getUserId(), e.getMessage());
            
            updateNotificationStatus(message, "FAILED");
            throw e;
        }
    }
    
    private void updateNotificationStatus(NotificationMessage message, String status) {
        Notification notification = Notification.builder()
            .userId(message.getUserId())
            .title(message.getTitle())
            .message(message.getMessage())
            .type(message.getType())
            .status(status)
            .createdAt(LocalDateTime.now())
            .build();
        
        if ("SENT".equals(status)) {
            notification.setSentAt(LocalDateTime.now());
        }
        
        notificationRepository.save(notification);
    }
}
```

## 🧪 테스트 가이드

### 1. 단위 테스트

#### 서비스 테스트
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ValidationService validationService;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("사용자 생성 성공")
    void createUser_Success() {
        // Given
        UserRequest request = UserRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("010-1234-5678")
            .build();
        
        User user = User.builder()
            .id("123")
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("010-1234-5678")
            .active(true)
            .build();
        
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        User result = userService.createUser(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.isActive()).isTrue();
        
        verify(validationService).validateUserRequest(request);
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("중복 이메일로 사용자 생성 실패")
    void createUser_DuplicateEmail_ThrowsException() {
        // Given
        UserRequest request = UserRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("010-1234-5678")
            .build();
        
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessage("이미 존재하는 이메일입니다: test@example.com");
        
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
}
```

#### 컨트롤러 테스트
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("사용자 생성 API 성공")
    void createUser_Success() throws Exception {
        // Given
        UserRequest request = UserRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("010-1234-5678")
            .build();
        
        User user = User.builder()
            .id("123")
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("010-1234-5678")
            .active(true)
            .build();
        
        when(userService.createUser(any(UserRequest.class))).thenReturn(user);
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("123"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.active").value(true));
    }
    
    @Test
    @DisplayName("사용자 생성 API - 유효하지 않은 요청")
    void createUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        UserRequest request = UserRequest.builder()
            .username("")
            .email("invalid-email")
            .phoneNumber("")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }
}
```

### 2. 통합 테스트

#### MongoDB 통합 테스트
```java
@DataMongoTest
@ExtendWith(SpringExtension.class)
class UserRepositoryIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    
    @Test
    @DisplayName("사용자 저장 및 조회")
    void saveAndFindUser() {
        // Given
        User user = User.builder()
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("010-1234-5678")
            .active(true)
            .build();
        
        // When
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().isActive()).isTrue();
    }
    
    @Test
    @DisplayName("이메일로 사용자 조회")
    void findByEmail() {
        // Given
        User user = User.builder()
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("010-1234-5678")
            .active(true)
            .build();
        
        userRepository.save(user);
        
        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }
}
```

#### Kafka 통합 테스트
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = {"notifications"})
class KafkaIntegrationTest {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private NotificationConsumer notificationConsumer;
    
    @Test
    @DisplayName("Kafka 메시지 전송 및 수신")
    void sendAndReceiveMessage() throws Exception {
        // Given
        String topic = "notifications";
        String key = "user123";
        String message = "{\"userId\":\"user123\",\"title\":\"테스트\",\"message\":\"테스트 메시지\",\"type\":\"EMAIL\"}";
        
        // When
        kafkaTemplate.send(topic, key, message);
        
        // Then
        // Consumer가 메시지를 처리할 시간을 주기 위해 잠시 대기
        Thread.sleep(2000);
        
        // 실제 테스트에서는 메시지 처리 결과를 검증
        // 예: 데이터베이스에 알림 기록이 저장되었는지 확인
    }
}
```

### 3. E2E 테스트

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class NotificationSystemE2ETest {
    
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");
    
    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        notificationRepository.deleteAll();
    }
    
    @Test
    @DisplayName("전체 알림 전송 플로우")
    void completeNotificationFlow() {
        // 1. 사용자 생성
        UserRequest userRequest = UserRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .phoneNumber("010-1234-5678")
            .build();
        
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
            "/api/users", userRequest, UserResponse.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String userId = createResponse.getBody().getId();
        
        // 2. 알림 전송
        String notificationUrl = String.format("/api/notifications/send/user/%s?title=테스트&message=테스트메시지&type=EMAIL", userId);
        ResponseEntity<String> notificationResponse = restTemplate.postForEntity(
            notificationUrl, null, String.class);
        
        assertThat(notificationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // 3. 알림 상태 확인 (잠시 대기 후)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ResponseEntity<List<Notification>> notificationsResponse = restTemplate.exchange(
            "/api/notifications/user/" + userId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Notification>>() {}
        );
        
        assertThat(notificationsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(notificationsResponse.getBody()).hasSize(1);
        assertThat(notificationsResponse.getBody().get(0).getStatus()).isEqualTo("SENT");
    }
}
```

## 🐛 디버깅

### 1. 로깅 설정

#### application-dev.properties
```properties
# 로깅 레벨 설정
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.kafka=DEBUG
logging.level.org.springframework.data.mongodb=DEBUG

# 로그 패턴
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# 파일 로깅
logging.file.name=logs/notification-system-dev.log
logging.file.max-size=10MB
logging.file.max-history=30
```

#### 로깅 사용법
```java
@Slf4j
@Service
public class UserService {
    
    public User createUser(UserRequest request) {
        log.info("사용자 생성 시작: email={}", request.getEmail());
        
        try {
            // 비즈니스 로직
            User user = userRepository.save(convertToUser(request));
            
            log.info("사용자 생성 완료: id={}, email={}", user.getId(), user.getEmail());
            return user;
            
        } catch (Exception e) {
            log.error("사용자 생성 실패: email={}, error={}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
}
```

### 2. 디버깅 도구

#### IntelliJ IDEA 디버깅
```java
// 브레이크포인트 설정
@Service
public class UserService {
    
    public User createUser(UserRequest request) {
        // 브레이크포인트를 여기에 설정
        log.info("사용자 생성 시작: {}", request.getEmail());
        
        // 변수 검사
        String email = request.getEmail();
        String username = request.getUsername();
        
        // 조건부 브레이크포인트: email이 "test@example.com"일 때만
        if ("test@example.com".equals(email)) {
            log.debug("테스트 이메일 발견");
        }
        
        return userRepository.save(convertToUser(request));
    }
}
```

#### MongoDB 디버깅
```javascript
// MongoDB Shell에서 실행
// 느린 쿼리 로깅 활성화
db.setProfilingLevel(1, 100); // 100ms 이상 쿼리 로깅

// 현재 실행 중인 쿼리 확인
db.currentOp()

// 느린 쿼리 확인
db.system.profile.find().sort({ts: -1}).limit(10)

// 특정 컬렉션의 인덱스 확인
db.users.getIndexes()

// 쿼리 실행 계획 확인
db.users.find({email: "test@example.com"}).explain("executionStats")
```

#### Kafka 디버깅
```bash
# 토픽 정보 확인
bin/kafka-topics.sh --describe --topic notifications --bootstrap-server localhost:9092

# Consumer Group 상태 확인
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group notification-group

# 토픽에서 메시지 확인
bin/kafka-console-consumer.sh --topic notifications --bootstrap-server localhost:9092 --from-beginning

# Producer로 메시지 전송 테스트
bin/kafka-console-producer.sh --topic notifications --bootstrap-server localhost:9092
```

### 3. 성능 프로파일링

#### JVM 프로파일링
```bash
# JVM 힙 덤프 생성
jmap -dump:format=b,file=heap.hprof <pid>

# 스레드 덤프 생성
jstack <pid> > thread.txt

# JVM 통계 확인
jstat -gc <pid> 1000 10
```

#### Spring Boot Actuator
```properties
# application.properties
management.endpoints.web.exposure.include=health,metrics,info,env,configprops
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

# 설정 확인
curl http://localhost:8080/actuator/configprops
```

## ⚡ 성능 최적화

### 1. 데이터베이스 최적화

#### 인덱스 최적화
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

#### 쿼리 최적화
```java
@Service
public class UserService {
    
    // 프로젝션 사용으로 필요한 필드만 조회
    public List<UserSummary> getUserSummaries() {
        Query query = new Query();
        query.fields().include("id").include("username").include("email").include("active");
        return mongoTemplate.find(query, UserSummary.class, "users");
    }
    
    // 배치 처리
    @Transactional
    public void processUsersInBatches() {
        int batchSize = 1000;
        int skip = 0;
        
        while (true) {
            Page<User> userPage = userRepository.findAll(
                PageRequest.of(skip / batchSize, batchSize)
            );
            
            if (userPage.isEmpty()) break;
            
            processUsers(userPage.getContent());
            skip += batchSize;
        }
    }
}
```

### 2. Kafka 최적화

#### Producer 최적화
```properties
# application.properties
spring.kafka.producer.batch-size=32768
spring.kafka.producer.buffer-memory=67108864
spring.kafka.producer.compression-type=snappy
spring.kafka.producer.linger.ms=10
spring.kafka.producer.acks=1
```

#### Consumer 최적화
```java
@Configuration
public class KafkaConsumerConfig {
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(6); // 파티션 수와 동일하게 설정
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }
}
```

### 3. 캐싱 최적화

#### Redis 캐싱
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}

@Service
public class UserService {
    
    @Cacheable("users")
    public User getUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + id));
    }
    
    @CacheEvict("users")
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
```

### 4. 비동기 처리 최적화

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Notification-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationService {
    
    @Async
    public CompletableFuture<Void> sendNotificationAsync(NotificationMessage message) {
        try {
            // 알림 전송 로직
            processNotification(message);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    public void sendBatchNotifications(List<NotificationMessage> messages) {
        List<CompletableFuture<Void>> futures = messages.stream()
            .map(this::sendNotificationAsync)
            .collect(Collectors.toList());
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .join();
    }
}
``` 