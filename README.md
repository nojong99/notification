# 🔔 Spring Boot 알림 시스템

Spring Boot, Spring Security, MongoDB를 사용한 권한 기반 알림 시스템입니다.

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시작하기](#-시작하기)
- [사용법](#-사용법)
- [API 문서](#-api-문서)
- [문제 해결](#-문제-해결)
- [최근 업데이트](#-최근-업데이트)
- [기여하기](#-기여하기)

## 🎯 프로젝트 개요

이 프로젝트는 Spring Boot를 기반으로 한 웹 기반 알림 시스템입니다. 관리자는 사용자들에게 알림을 보낼 수 있고, 일반 사용자는 자신의 알림을 확인할 수 있습니다.

### 시스템 구조

```
알림 시스템
├── 웹 인터페이스 (Thymeleaf + Bootstrap)
├── Spring Boot 백엔드
├── MongoDB (데이터 영속성)
├── Spring Security (로그인/권한 관리)
└── 알림 관리 시스템
```

## ✨ 주요 기능

### 🔐 인증 및 권한 관리
- ✅ 회원가입/로그인/로그아웃
- ✅ Spring Security 기반 인증
- ✅ 역할 기반 권한 제어 (ADMIN/USER)
- ✅ 비밀번호 암호화 (BCrypt)
- ✅ MongoDB 사용자 연동

### 🔔 알림 시스템
- ✅ 개별 사용자 알림 전송
- ✅ 전체 사용자 알림 전송
- ✅ 알림 상태 관리 (PENDING/SENT/FAILED/READ)
- ✅ 알림 타입 분류 (INFO/WARNING/ERROR)
- ✅ 알림 재전송 및 삭제
- ✅ 실시간 알림 개수 표시
- ✅ 로컬 알림 처리 (Kafka 제거)

### 👥 사용자 관리
- ✅ 사용자 목록 조회
- ✅ 사용자 활성화/비활성화
- ✅ 사용자 프로필 관리
- ✅ 사용자별 알림 확인
- ✅ 마이페이지 기능

### 🌐 웹 인터페이스
- ✅ 반응형 웹 디자인
- ✅ 실시간 통계 표시
- ✅ 사용자별 대시보드
- ✅ 관리자 전용 기능
- ✅ 실시간 알림 업데이트
- ✅ Thymeleaf 보안 최적화

## 🛠️ 기술 스택

### 백엔드
- **Java 17**
- **Spring Boot 3.5.3**
- **Spring Security**
- **Spring Data MongoDB**
- **MongoDB 6.0**
- **Gradle**

### 프론트엔드
- **Thymeleaf**
- **Bootstrap 5**
- **Font Awesome**
- **JavaScript**

### 개발 도구
- **IntelliJ IDEA** / **VS Code**
- **Git**
- **Docker** (선택사항)

## 🚀 시작하기

### 필수 요구사항

- Java JDK 17 이상
- MongoDB 6.0 이상 (또는 Docker)
- Gradle (또는 프로젝트에 포함된 gradlew 사용)
- 웹 브라우저

### MongoDB 설치

#### 방법 1: Docker 사용 (권장)

1. **Docker Compose 실행**
   ```bash
   docker-compose up -d
   ```

2. **MongoDB 상태 확인**
   ```bash
   docker ps
   ```

#### 방법 2: 로컬 MongoDB 설치

1. [MongoDB Community Server](https://www.mongodb.com/try/download/community) 다운로드
2. 설치 및 서비스 시작
3. MongoDB Compass로 연결 확인

### 설치 및 실행

1. **프로젝트 클론**
   ```bash
   git clone <repository-url>
   cd play
   ```

2. **프로젝트 빌드**
   ```bash
   # Windows
   gradlew build
   
   # Mac/Linux
   ./gradlew build
   ```

3. **애플리케이션 실행**
   ```bash
   # Windows
   gradlew bootRun
   
   # Mac/Linux
   ./gradlew bootRun
   ```

4. **브라우저에서 접속**
   ```
   http://localhost:8081
   ```

### 기본 계정

- **관리자**: `admin` / `admin123`
- **일반 사용자**: `user1` / `user123`

## 📖 사용법

### 관리자 기능

1. **관리자로 로그인**
2. **알림 작성**: `/notifications/create`
3. **알림 관리**: `/notifications/list`
4. **사용자 관리**: `/users/list`

### 일반 사용자 기능

1. **로그인**
2. **마이페이지**: `/mypage`
3. **알림 확인**: `/mypage/notifications`
4. **프로필 관리**: `/mypage/profile`

## 🔧 API 문서

### 기본 API

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| GET | `/api/test/health` | 서버 상태 확인 |
| GET | `/api/test/info` | 시스템 정보 |
| GET | `/api/test/stats` | 통계 정보 |

### 알림 API

| 메서드 | 엔드포인트 | 설명 | 권한 |
|--------|------------|------|------|
| POST | `/api/notifications/send/user/{userId}` | 개별 사용자 알림 전송 | ADMIN |
| POST | `/api/notifications/send/all` | 전체 사용자 알림 전송 | ADMIN |
| GET | `/api/notifications/user/{userId}` | 사용자 알림 조회 | USER |
| GET | `/api/notifications/status/{status}` | 상태별 알림 조회 | ADMIN |

### 마이페이지 API

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| GET | `/mypage/notifications/count` | 읽지 않은 알림 개수 |
| POST | `/mypage/notifications/{id}/read` | 알림 읽음 처리 |
| POST | `/mypage/notifications/read-all` | 모든 알림 읽음 처리 |

## 🗄️ 데이터베이스

### MongoDB 연결 정보

- **호스트**: `localhost`
- **포트**: `27017`
- **데이터베이스**: `notification_system`
- **컬렉션**: `users`, `notifications`

### MongoDB Compass 접속

- **연결 문자열**: `mongodb://localhost:27017`
- **데이터베이스**: `notification_system`

### 주요 컬렉션

- **users**: 사용자 정보
- **notifications**: 알림 정보

## 🚨 문제 해결

### 자주 발생하는 문제

1. **MongoDB 연결 오류**
   ```bash
   # MongoDB 서비스 상태 확인
   docker ps
   
   # 또는 로컬 MongoDB 서비스 확인
   net start MongoDB
   ```

2. **포트 충돌**
   ```properties
   # application.properties
   server.port=8082
   ```

3. **로그인 실패**
   - 계정 정보 확인
   - MongoDB에서 사용자 데이터 확인

4. **빌드 오류**
   - Java 17 이상 설치 확인
   - Gradle 의존성 다운로드 확인

5. **Thymeleaf 템플릿 오류**
   - `th:onclick` 대신 `data-*` 속성 사용
   - 복잡한 표현식 단순화

### 디버깅

- **로그 확인**: 애플리케이션 실행 시 콘솔 로그 확인
- **MongoDB Compass**: 데이터베이스 상태 확인
- **브라우저 개발자 도구**: 네트워크 요청 및 오류 확인

## 📝 최근 업데이트

### v1.2.0 (2025-07-18)

#### 🔧 주요 수정사항

1. **Thymeleaf 보안 문제 해결**
   - `th:onclick` 속성을 `data-*` 속성으로 변경
   - JavaScript에서 `this.dataset.*`를 통해 데이터 접근
   - 복잡한 `subList` 표현식 제거

2. **MongoDB 연동 최적화**
   - H2에서 MongoDB로 완전 전환
   - JPA 엔티티를 MongoDB Document로 변경
   - ID 타입을 Long에서 String으로 변경
   - 순환 참조 문제 해결

3. **컨트롤러 개선**
   - 데이터 미리 처리로 템플릿 복잡도 감소
   - `recentNotifications`와 `mainNotifications` 분리
   - 예외 처리 강화

4. **템플릿 최적화**
   - 모든 템플릿에서 보안 문제 해결
   - 반응형 디자인 개선
   - 실시간 알림 업데이트 기능

#### 🐛 해결된 문제들

- ✅ 화이트라벨 오류 페이지 문제
- ✅ Thymeleaf 템플릿 파싱 오류
- ✅ MongoDB 연결 및 데이터 조회 문제
- ✅ 순환 참조 오류
- ✅ 포트 충돌 문제

#### 📁 수정된 파일들

- `src/main/resources/templates/notification/list.html`
- `src/main/resources/templates/user/list.html`
- `src/main/resources/templates/mypage/notifications.html`
- `src/main/resources/templates/mypage/index.html`
- `src/main/java/com/example/demo/controller/MyPageController.java`
- `src/main/java/com/example/demo/model/User.java`
- `src/main/java/com/example/demo/model/Notification.java`
- `src/main/java/com/example/demo/repository/UserRepository.java`
- `src/main/java/com/example/demo/repository/NotificationRepository.java`

### v1.1.0 (2025-07-17)

- Kafka 의존성 제거
- 로컬 알림 처리 시스템 구현
- H2 데이터베이스에서 MongoDB로 전환
- Spring Security 설정 개선

### v1.0.0 (2025-07-16)

- 초기 프로젝트 생성
- 기본 알림 시스템 구현
- Spring Security 인증 시스템
- Thymeleaf 웹 인터페이스

## 📚 상세 문서

프로젝트의 상세한 구현 과정과 가이드는 다음 문서들을 참조하세요:

- [📋 프로젝트 개요 및 준비사항](./docs/01-project-overview.md)
- [🚀 Spring Boot 프로젝트 생성 및 기본 설정](./docs/02-project-setup.md)
- [🗄️ MongoDB 설정 및 데이터베이스 구성](./docs/10-mongodb-setup.md)

## 🔄 데이터 영속성

### MongoDB 데이터 구조

#### User Collection
```json
{
  "_id": "ObjectId",
  "username": "string",
  "email": "string",
  "phoneNumber": "string",
  "password": "string (encrypted)",
  "role": "ADMIN|USER",
  "active": "boolean",
  "createdAt": "datetime"
}
```

#### Notification Collection
```json
{
  "_id": "ObjectId",
  "userId": "string",
  "title": "string",
  "message": "string",
  "type": "INFO|WARNING|ERROR",
  "status": "PENDING|SENT|FAILED|READ",
  "createdAt": "datetime",
  "sentAt": "datetime"
}
```

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 📞 연락처

프로젝트 링크: [https://github.com/yourusername/play](https://github.com/yourusername/play)

---

⭐ 이 프로젝트가 도움이 되었다면 스타를 눌러주세요! 