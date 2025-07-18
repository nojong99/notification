# 🔧 문제 해결 및 디버깅

## 🚨 자주 발생하는 문제들과 해결 방법

### 1. 빌드 오류

#### 문제: Java 버전 오류
```
Error: Java 17 or higher is required
```
**해결 방법:**
- Java 17 이상을 설치하세요
- `java -version` 명령어로 버전 확인

#### 문제: Gradle 의존성 다운로드 실패
```
Could not download gradle-wrapper.jar
```
**해결 방법:**
- 인터넷 연결 확인
- 방화벽 설정 확인
- Gradle 캐시 삭제: `gradlew clean`

### 2. 애플리케이션 시작 오류

#### 문제: 포트 충돌
```
Web server failed to start. Port 8081 was already in use.
```
**해결 방법:**
1. 실행 중인 프로세스 종료:
   ```cmd
   taskkill /f /im java.exe
   ```
2. 또는 `application.properties`에서 포트 변경:
   ```properties
   server.port=8082
   ```

#### 문제: 데이터베이스 연결 실패
```
Database not found
```
**해결 방법:**
- `application.properties`의 데이터베이스 설정 확인
- H2 콘솔 접속 테스트: `http://localhost:8081/h2-console`

### 3. 로그인 문제

#### 문제: 로그인 실패
```
Invalid username or password
```
**해결 방법:**
1. H2 콘솔에서 사용자 계정 확인
2. 비밀번호 인코딩 확인
3. 테스트 계정으로 로그인 시도:
   - 관리자: `admin` / `admin123`
   - 사용자: `user1` / `password1`

#### 문제: 권한 오류
```
Access Denied
```
**해결 방법:**
- 사용자 역할 확인 (ADMIN vs USER)
- Spring Security 설정 확인

### 4. 웹 페이지 오류

#### 문제: Thymeleaf 템플릿 오류
```
Could not resolve view with name 'index'
```
**해결 방법:**
- 템플릿 파일 경로 확인
- 파일명과 확장자 확인
- UTF-8 인코딩 확인

#### 문제: CSS/JS 로딩 실패
```
Failed to load resource
```
**해결 방법:**
- 정적 리소스 경로 확인
- 브라우저 캐시 삭제
- 개발자 도구에서 네트워크 탭 확인

## 🔍 디버깅 방법

### 1. 로그 확인

애플리케이션 로그를 확인하여 오류 원인을 파악하세요:

```bash
# Windows
gradlew bootRun

# Mac/Linux
./gradlew bootRun
```

### 2. H2 데이터베이스 확인

H2 콘솔에서 데이터 확인:

1. 브라우저에서 `http://localhost:8081/h2-console` 접속
2. JDBC URL: `jdbc:h2:mem:testdb`
3. 사용자명: `sa`
4. 비밀번호: (비어있음)

### 3. API 테스트

curl 명령어로 API 테스트:

```bash
# 서버 상태 확인
curl http://localhost:8081/api/test/health

# 통계 확인
curl http://localhost:8081/api/test/stats
```

### 4. 브라우저 개발자 도구

F12를 눌러 개발자 도구에서:
- Console 탭: JavaScript 오류 확인
- Network 탭: 요청/응답 확인
- Elements 탭: HTML 구조 확인

## 🛠️ 문제 해결 체크리스트

### 빌드 문제
- [ ] Java 17 이상 설치됨
- [ ] Gradle 의존성 다운로드 완료
- [ ] 프로젝트 구조 올바름

### 실행 문제
- [ ] 포트 8081 사용 가능
- [ ] 데이터베이스 설정 올바름
- [ ] Spring Security 설정 완료

### 로그인 문제
- [ ] 사용자 계정 데이터베이스에 존재
- [ ] 비밀번호 인코딩 올바름
- [ ] Spring Security 설정 확인

### 웹 페이지 문제
- [ ] 템플릿 파일 경로 올바름
- [ ] 정적 리소스 경로 올바름
- [ ] 브라우저 캐시 삭제

## 📞 추가 도움

문제가 해결되지 않으면:

1. **로그 메시지** 전체를 확인하세요
2. **오류 메시지**를 정확히 기록하세요
3. **단계별로** 문제를 재현해보세요
4. **브라우저 개발자 도구**를 활용하세요

## 🚀 다음 단계

문제 해결이 완료되면 다음 문서로 이동하세요:
👉 **[실행 및 테스트 가이드](./09-testing-guide.md)** 