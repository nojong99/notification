# API 문서 (API Documentation)

사용자 알림 시스템의 REST API 엔드포인트 문서입니다.

## 📋 목차

- [기본 정보](#기본-정보)
- [인증](#인증)
- [사용자 API](#사용자-api)
- [알림 API](#알림-api)
- [응답 코드](#응답-코드)
- [에러 처리](#에러-처리)

## 🌐 기본 정보

### Base URL
```
http://localhost:8080/api
```

### Content-Type
```
application/json
```

### API 버전
현재 버전: `v1`

## 🔐 인증

현재 버전에서는 인증이 필요하지 않습니다. (JWT 미사용)

## 👥 사용자 API

### 사용자 생성

새로운 사용자를 생성합니다.

**Endpoint:** `POST /users`

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "phoneNumber": "string"
}
```

**Request Parameters:**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| username | string | ✅ | 사용자명 |
| email | string | ✅ | 이메일 주소 |
| phoneNumber | string | ❌ | 전화번호 |

**Response (200 OK):**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "username": "testuser",
  "email": "test@example.com",
  "phoneNumber": "010-1234-5678",
  "active": true
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "phoneNumber": "010-1234-5678"
  }'
```

---

### 모든 사용자 조회

시스템에 등록된 모든 사용자를 조회합니다.

**Endpoint:** `GET /users`

**Response (200 OK):**
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "username": "user1",
    "email": "user1@example.com",
    "phoneNumber": "010-1111-1111",
    "active": true
  },
  {
    "id": "507f1f77bcf86cd799439012",
    "username": "user2",
    "email": "user2@example.com",
    "phoneNumber": "010-2222-2222",
    "active": false
  }
]
```

**Example:**
```bash
curl http://localhost:8080/api/users
```

---

### 사용자 ID로 조회

특정 사용자의 정보를 조회합니다.

**Endpoint:** `GET /users/{id}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | string | 사용자 ID |

**Response (200 OK):**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "username": "testuser",
  "email": "test@example.com",
  "phoneNumber": "010-1234-5678",
  "active": true
}
```

**Response (404 Not Found):**
```json
{
  "error": "사용자를 찾을 수 없습니다: 507f1f77bcf86cd799439011"
}
```

**Example:**
```bash
curl http://localhost:8080/api/users/507f1f77bcf86cd799439011
```

---

### 활성 사용자 조회

활성 상태인 사용자만 조회합니다.

**Endpoint:** `GET /users/active`

**Response (200 OK):**
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "username": "user1",
    "email": "user1@example.com",
    "phoneNumber": "010-1111-1111",
    "active": true
  }
]
```

**Example:**
```bash
curl http://localhost:8080/api/users/active
```

---

### 사용자 정보 수정

사용자 정보를 수정합니다.

**Endpoint:** `PUT /users/{id}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | string | 사용자 ID |

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "phoneNumber": "string",
  "active": boolean
}
```

**Response (200 OK):**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "username": "updateduser",
  "email": "updated@example.com",
  "phoneNumber": "010-9999-9999",
  "active": false
}
```

**Example:**
```bash
curl -X PUT http://localhost:8080/api/users/507f1f77bcf86cd799439011 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "updateduser",
    "email": "updated@example.com",
    "phoneNumber": "010-9999-9999",
    "active": false
  }'
```

---

### 사용자 삭제

사용자를 삭제합니다.

**Endpoint:** `DELETE /users/{id}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | string | 사용자 ID |

**Response (204 No Content):**
```
(응답 본문 없음)
```

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/users/507f1f77bcf86cd799439011
```

## 🔔 알림 API

### 특정 사용자에게 알림 전송

특정 사용자에게 알림을 전송합니다.

**Endpoint:** `POST /notifications/send/user/{userId}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| userId | string | 사용자 ID |

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| title | string | ✅ | 알림 제목 |
| message | string | ✅ | 알림 내용 |
| type | string | ✅ | 알림 타입 (EMAIL, SMS, PUSH) |

**Response (200 OK):**
```
알림이 큐에 추가되었습니다.
```

**Example:**
```bash
curl -X POST "http://localhost:8080/api/notifications/send/user/507f1f77bcf86cd799439011?title=환영합니다&message=시스템에 오신 것을 환영합니다&type=EMAIL"
```

---

### 모든 사용자에게 알림 전송

활성 상태인 모든 사용자에게 알림을 전송합니다.

**Endpoint:** `POST /notifications/send/all`

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| title | string | ✅ | 알림 제목 |
| message | string | ✅ | 알림 내용 |
| type | string | ✅ | 알림 타입 (EMAIL, SMS, PUSH) |

**Response (200 OK):**
```
모든 사용자에게 알림이 큐에 추가되었습니다.
```

**Example:**
```bash
curl -X POST "http://localhost:8080/api/notifications/send/all?title=공지사항&message=시스템 점검이 있습니다&type=SMS"
```

---

### 사용자별 알림 조회

특정 사용자의 알림 히스토리를 조회합니다.

**Endpoint:** `GET /notifications/user/{userId}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| userId | string | 사용자 ID |

**Response (200 OK):**
```json
[
  {
    "id": "507f1f77bcf86cd799439021",
    "userId": "507f1f77bcf86cd799439011",
    "title": "환영합니다",
    "message": "시스템에 오신 것을 환영합니다",
    "type": "EMAIL",
    "status": "SENT",
    "createdAt": "2024-01-01T12:00:00",
    "sentAt": "2024-01-01T12:00:05"
  }
]
```

**Example:**
```bash
curl http://localhost:8080/api/notifications/user/507f1f77bcf86cd799439011
```

---

### 상태별 알림 조회

특정 상태의 알림들을 조회합니다.

**Endpoint:** `GET /notifications/status/{status}`

**Path Parameters:**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| status | string | 알림 상태 (PENDING, SENT, FAILED) |

**Response (200 OK):**
```json
[
  {
    "id": "507f1f77bcf86cd799439021",
    "userId": "507f1f77bcf86cd799439011",
    "title": "환영합니다",
    "message": "시스템에 오신 것을 환영합니다",
    "type": "EMAIL",
    "status": "SENT",
    "createdAt": "2024-01-01T12:00:00",
    "sentAt": "2024-01-01T12:00:05"
  }
]
```

**Example:**
```bash
curl http://localhost:8080/api/notifications/status/SENT
```

## 📊 응답 코드

| 코드 | 설명 | 사용 예시 |
|------|------|-----------|
| 200 | OK | 요청 성공 |
| 201 | Created | 리소스 생성 성공 |
| 204 | No Content | 요청 성공 (응답 본문 없음) |
| 400 | Bad Request | 잘못된 요청 |
| 404 | Not Found | 리소스를 찾을 수 없음 |
| 500 | Internal Server Error | 서버 내부 오류 |

## ❌ 에러 처리

### 에러 응답 형식

```json
{
  "error": "에러 메시지",
  "timestamp": "2024-01-01T12:00:00",
  "path": "/api/users/123"
}
```

### 일반적인 에러

#### 400 Bad Request
```json
{
  "error": "필수 필드가 누락되었습니다: email"
}
```

#### 404 Not Found
```json
{
  "error": "사용자를 찾을 수 없습니다: 507f1f77bcf86cd799439011"
}
```

#### 500 Internal Server Error
```json
{
  "error": "서버 내부 오류가 발생했습니다",
  "timestamp": "2024-01-01T12:00:00",
  "path": "/api/notifications/send/user/123"
}
```

## 📝 알림 타입

| 타입 | 설명 | 사용 예시 |
|------|------|-----------|
| EMAIL | 이메일 알림 | 환영 메일, 비밀번호 재설정 |
| SMS | SMS 알림 | 인증번호, 긴급 공지 |
| PUSH | 푸시 알림 | 앱 알림, 실시간 메시지 |

## 📈 알림 상태

| 상태 | 설명 |
|------|------|
| PENDING | 대기 중 (Kafka 큐에 추가됨) |
| SENT | 전송 완료 |
| FAILED | 전송 실패 |

## 🔧 API 테스트

### Postman Collection

API 테스트를 위한 Postman Collection을 제공합니다:

```json
{
  "info": {
    "name": "Notification System API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Users",
      "item": [
        {
          "name": "Create User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"testuser\",\n  \"email\": \"test@example.com\",\n  \"phoneNumber\": \"010-1234-5678\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/users",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "users"]
            }
          }
        }
      ]
    }
  ]
}
```

### cURL 예시

모든 API 엔드포인트에 대한 cURL 예시는 각 섹션에서 확인할 수 있습니다. 