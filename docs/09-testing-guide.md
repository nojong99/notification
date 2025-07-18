# 🧪 실행 및 테스트 가이드

## 🚀 애플리케이션 실행

### 1단계: 프로젝트 빌드

#### Windows에서:
```cmd
# 프로젝트 폴더로 이동
cd D:\test\play

# Gradle 빌드
gradlew build

# 성공 메시지 확인
BUILD SUCCESSFUL
```

#### Mac/Linux에서:
```bash
# 프로젝트 폴더로 이동
cd ~/projects/play

# Gradle 빌드
./gradlew build

# 성공 메시지 확인
BUILD SUCCESSFUL
```

### 2단계: 애플리케이션 실행

#### Windows에서:
```cmd
# 애플리케이션 실행
gradlew bootRun
```

#### Mac/Linux에서:
```bash
# 애플리케이션 실행
./gradlew bootRun
```

### 3단계: 실행 확인

애플리케이션이 성공적으로 시작되면 다음 메시지들이 나타납니다:

```
Started PlayApplication in X.XXX seconds
Tomcat started on port(s): 8081 (http)
Started PlayApplication
```

## 🌐 웹 브라우저 테스트

### 1단계: 메인 페이지 접속

브라우저에서 `http://localhost:8081` 접속

**확인 사항:**
- ✅ 페이지가 정상적으로 로드됨
- ✅ "알림 시스템" 제목 표시
- ✅ 로그인/회원가입 버튼 표시

### 2단계: 회원가입 테스트

1. **회원가입 페이지 접속**: `http://localhost:8081/register`
2. **새 계정 생성**:
   - 사용자명: `testuser`
   - 비밀번호: `testpass123`
   - 이메일: `test@example.com`
   - 전화번호: `010-9999-9999`
3. **회원가입 버튼 클릭**
4. **성공 메시지 확인**

### 3단계: 로그인 테스트

#### 새로 만든 계정으로 로그인
1. **로그인 페이지 접속**: `http://localhost:8081/login`
2. **계정 정보 입력**:
   - 사용자명: `testuser`
   - 비밀번호: `testpass123`
3. **로그인 버튼 클릭**
4. **대시보드로 이동 확인**

#### 기존 테스트 계정으로 로그인
- **관리자 계정**: `admin` / `admin123`
- **일반 사용자**: `user1` / `password1`

### 4단계: 대시보드 테스트

1. **대시보드 접속**: `http://localhost:8081/dashboard`
2. **확인 사항**:
   - ✅ 사용자명과 환영 메시지 표시
   - ✅ 시스템 통계 카드 표시
   - ✅ 빠른 액션 버튼 표시

### 5단계: 권한별 기능 테스트

#### 관리자 계정으로 테스트
1. **관리자로 로그인**: `admin` / `admin123`
2. **관리자 메뉴 확인**:
   - ✅ 알림 작성 메뉴 표시
   - ✅ 알림 목록 메뉴 표시
   - ✅ 사용자 관리 메뉴 표시

#### 일반 사용자 계정으로 테스트
1. **일반 사용자로 로그인**: `user1` / `password1`
2. **일반 사용자 메뉴 확인**:
   - ✅ 관리자 메뉴 숨김
   - ✅ 마이페이지 메뉴 표시

## 🔧 API 테스트

### 1단계: 서버 상태 확인

```bash
# 서버 상태 확인
curl http://localhost:8081/api/test/health

# 예상 응답:
{
  "status": "OK",
  "message": "서버가 정상적으로 실행 중입니다.",
  "timestamp": 1234567890
}
```

### 2단계: 시스템 정보 확인

```bash
# 시스템 정보 확인
curl http://localhost:8081/api/test/info

# 예상 응답:
{
  "application": "알림 시스템",
  "version": "1.0.0",
  "java.version": "17.0.x",
  "os.name": "Windows 10"
}
```

### 3단계: 통계 정보 확인

```bash
# 통계 정보 확인
curl http://localhost:8081/api/test/stats

# 예상 응답:
{
  "totalUsers": 4,
  "activeUsers": 4,
  "totalNotifications": 0,
  "pendingNotifications": 0,
  "sentNotifications": 0,
  "failedNotifications": 0
}
```

## 🗄️ 데이터베이스 테스트

### 1단계: H2 콘솔 접속

1. **브라우저에서 접속**: `http://localhost:8081/h2-console`
2. **연결 정보 입력**:
   - JDBC URL: `jdbc:h2:mem:testdb`
   - 사용자명: `sa`
   - 비밀번호: (비어있음)
3. **Connect 버튼 클릭**

### 2단계: 데이터 확인

#### 사용자 테이블 확인
```sql
SELECT * FROM USERS;
```

**예상 결과:**
- `admin` 계정 (ADMIN 권한)
- `user1`, `user2`, `user3` 계정 (USER 권한)
- 새로 만든 `testuser` 계정

#### 알림 테이블 확인
```sql
SELECT * FROM NOTIFICATIONS;
```

## 🔔 알림 기능 테스트

### 1단계: 알림 작성 (관리자만)

1. **관리자로 로그인**: `admin` / `admin123`
2. **알림 작성 페이지 접속**: `http://localhost:8081/notifications/create`
3. **알림 정보 입력**:
   - 제목: `테스트 알림`
   - 내용: `이것은 테스트 알림입니다.`
   - 타입: `INFO`
   - 대상: `user1`
4. **전송 버튼 클릭**

### 2단계: 알림 확인

1. **user1로 로그인**: `user1` / `password1`
2. **마이페이지 접속**: `http://localhost:8081/mypage`
3. **알림 목록 확인**

## 🧹 정리 및 종료

### 애플리케이션 종료

터미널에서 `Ctrl + C`를 눌러 애플리케이션을 종료하세요.

### 프로세스 강제 종료 (필요시)

#### Windows에서:
```cmd
taskkill /f /im java.exe
```

#### Mac/Linux에서:
```bash
pkill -f java
```

## ✅ 테스트 체크리스트

### 기본 기능
- [ ] 애플리케이션 빌드 성공
- [ ] 애플리케이션 실행 성공
- [ ] 메인 페이지 접속 가능
- [ ] 회원가입 기능 정상
- [ ] 로그인 기능 정상
- [ ] 로그아웃 기능 정상

### 권한 기능
- [ ] 관리자 권한 확인
- [ ] 일반 사용자 권한 확인
- [ ] 권한별 메뉴 표시
- [ ] 권한 없는 페이지 접근 제한

### 데이터베이스
- [ ] H2 콘솔 접속 가능
- [ ] 사용자 데이터 확인
- [ ] 알림 데이터 확인

### API 기능
- [ ] 서버 상태 API 정상
- [ ] 시스템 정보 API 정상
- [ ] 통계 API 정상

## 🎉 축하합니다!

모든 테스트가 성공적으로 완료되면 알림 시스템이 정상적으로 작동하고 있습니다!

## 📚 추가 학습

이제 다음을 시도해보세요:

1. **새로운 기능 추가**: 알림 타입 추가, 사용자 프로필 수정 등
2. **UI 개선**: 더 예쁜 디자인, 애니메이션 추가
3. **기능 확장**: 이메일 알림, 푸시 알림 등
4. **데이터베이스 변경**: MySQL, PostgreSQL 등으로 변경

---

## 💡 팁

- **테스트는 단계별로** 진행하세요
- **오류가 발생하면** 로그를 꼭 확인하세요
- **브라우저 캐시**를 주기적으로 삭제하세요
- **개발자 도구**를 활용하여 디버깅하세요 