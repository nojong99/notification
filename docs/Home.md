# 사용자 알림 시스템 (User Notification System)

> MongoDB와 Apache Kafka를 활용한 확장 가능한 사용자 알림 시스템

## 📋 목차

- [시작하기](Getting-Started)
- [아키텍처 개요](Architecture-Overview)
- [API 문서](API-Documentation)
- [설치 및 설정](Installation-Guide)
- [MongoDB 가이드](MongoDB-Guide)
- [Kafka 가이드](Kafka-Guide)
- [개발 가이드](Development-Guide)
- [트러블슈팅](Troubleshooting)

## 🚀 빠른 시작

### 시스템 요구사항

- **Java**: 17 이상
- **MongoDB**: 4.4 이상
- **Apache Kafka**: 2.8 이상
- **Spring Boot**: 3.5.3

### 빠른 실행

```bash
# 1. MongoDB 시작
mongod

# 2. Kafka 시작
bin/kafka-server-start.sh config/server.properties

# 3. 애플리케이션 실행
./gradlew bootRun
```

### 첫 번째 알림 전송

```bash
# 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "phoneNumber": "010-1234-5678"
  }'

# 알림 전송
curl -X POST "http://localhost:8080/api/notifications/send/user/{userId}?title=환영합니다&message=시스템에 오신 것을 환영합니다&type=EMAIL"
```

## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │   Kafka         │    │   MongoDB       │
│   Controller    │───▶│   Producer      │───▶│   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Service       │    │   Kafka         │    │   Repository    │
│   Layer         │    │   Consumer      │    │   Layer         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## ✨ 주요 기능

### 🔔 알림 시스템
- **다중 채널 지원**: 이메일, SMS, 푸시 알림
- **비동기 처리**: Kafka를 통한 고성능 메시지 처리
- **상태 추적**: 알림 전송 상태 실시간 모니터링
- **대량 전송**: 모든 사용자에게 일괄 알림 전송

### 👥 사용자 관리
- **사용자 CRUD**: 완전한 사용자 관리 기능
- **활성 상태 관리**: 사용자 활성/비활성 상태 관리
- **연락처 정보**: 이메일, 전화번호 등 연락처 관리

### 📊 데이터 관리
- **MongoDB 저장**: NoSQL 데이터베이스 활용
- **알림 히스토리**: 모든 알림 전송 기록 보관
- **상태별 조회**: 전송 상태별 알림 조회

## 🛠️ 기술 스택

| 기술 | 버전 | 용도 |
|------|------|------|
| Spring Boot | 3.5.3 | 웹 애플리케이션 프레임워크 |
| MongoDB | 4.4+ | NoSQL 데이터베이스 |
| Apache Kafka | 2.8+ | 메시지 큐 시스템 |
| Java | 17+ | 프로그래밍 언어 |
| Gradle | 8.0+ | 빌드 도구 |

## 📈 성능 특징

- **고처리량**: 초당 수천 건의 알림 처리
- **확장성**: 수평 확장 가능한 아키텍처
- **내구성**: 메시지 손실 방지
- **실시간성**: 즉시 알림 전송 및 상태 업데이트

## 🤝 기여하기

프로젝트에 기여하고 싶으시다면 [개발 가이드](Development-Guide)를 참고해주세요.

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

---

**더 자세한 정보는 위의 목차를 참고하세요.** 