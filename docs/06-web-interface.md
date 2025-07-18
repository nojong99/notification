# 🌐 웹 인터페이스 구현

## 📝 1단계: WebController 생성

메인 페이지와 대시보드를 처리하는 컨트롤러를 만들어요.

`src/main/java/com/example/demo/controller/WebController.java` 파일을 생성:

```java
package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class WebController {
    
    private final UserService userService;
    
    public WebController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/")
    public String index(Model model) {
        // 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = null;
        boolean isAdmin = false;
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            currentUsername = authentication.getName();
            isAdmin = userService.isAdmin(currentUsername);
        }
        
        // 실제 데이터베이스에서 가져오기
        List<User> users = userService.getAllUsers();
        List<User> activeUsers = userService.getActiveUsers();
        
        model.addAttribute("title", "알림 시스템");
        model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("activeUsers", activeUsers.size());
        model.addAttribute("users", activeUsers);
        
        // 로그인 정보
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isLoggedIn", currentUsername != null);
        
        return "index";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = null;
        boolean isAdmin = false;
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            currentUsername = authentication.getName();
            isAdmin = userService.isAdmin(currentUsername);
        }
        
        // 실제 데이터베이스에서 가져오기
        List<User> users = userService.getAllUsers();
        List<User> activeUsers = userService.getActiveUsers();
        
        model.addAttribute("title", "대시보드 - 알림 시스템");
        model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("activeUsers", activeUsers.size());
        model.addAttribute("users", activeUsers);
        
        // 로그인 정보
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isLoggedIn", currentUsername != null);
        
        return "dashboard";
    }
}
```

## 📝 2단계: 메인 페이지 템플릿 수정

`src/main/resources/templates/index.html` 파일을 수정하여 로그인 상태에 따라 다른 내용을 표시하도록 해요.

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${title}">알림 시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .hero-section {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 4rem 0;
        }
        .feature-card {
            transition: transform 0.3s ease;
            border: none;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }
        .feature-card:hover {
            transform: translateY(-5px);
        }
        .stats-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 15px;
            padding: 2rem;
            margin-bottom: 2rem;
        }
        .btn-action {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            border-radius: 10px;
            padding: 12px 24px;
            color: white;
            font-weight: 600;
            transition: all 0.3s ease;
        }
        .btn-action:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
            color: white;
        }
    </style>
</head>
<body>
    <!-- 네비게이션 바 -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">
                <i class="fas fa-bell me-2"></i>알림 시스템
            </a>
            
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            
            <div class="collapse navbar-collapse" id="navbarNav">
                <!-- 로그인하지 않은 사용자용 메뉴 -->
                <ul class="navbar-nav me-auto" th:if="${!isLoggedIn}">
                    <li class="nav-item">
                        <a class="nav-link" href="/login">로그인</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/register">회원가입</a>
                    </li>
                </ul>
                
                <!-- 로그인한 사용자용 메뉴 -->
                <ul class="navbar-nav me-auto" th:if="${isLoggedIn}">
                    <li class="nav-item">
                        <a class="nav-link" href="/dashboard">대시보드</a>
                    </li>
                    <li class="nav-item" th:if="${isAdmin}">
                        <a class="nav-link" href="/notifications/create">알림 작성</a>
                    </li>
                    <li class="nav-item" th:if="${isAdmin}">
                        <a class="nav-link" href="/notifications/list">알림 목록</a>
                    </li>
                    <li class="nav-item" th:if="${isAdmin}">
                        <a class="nav-link" href="/users/list">사용자 관리</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/mypage">마이페이지</a>
                    </li>
                </ul>
                
                <!-- 로그인/로그아웃 섹션 -->
                <ul class="navbar-nav">
                    <li class="nav-item" th:if="${!isLoggedIn}">
                        <a class="nav-link" href="/login">
                            <i class="fas fa-sign-in-alt me-1"></i>로그인
                        </a>
                    </li>
                    <li class="nav-item" th:if="${!isLoggedIn}">
                        <a class="nav-link" href="/register">
                            <i class="fas fa-user-plus me-1"></i>회원가입
                        </a>
                    </li>
                    <li class="nav-item" th:if="${isLoggedIn}">
                        <span class="navbar-text me-3">
                            <i class="fas fa-user me-1"></i>
                            <span th:text="${currentUsername} + '님 환영합니다'">사용자님 환영합니다</span>
                        </span>
                    </li>
                    <li class="nav-item" th:if="${isLoggedIn}">
                        <form th:action="@{/logout}" method="post" class="d-inline">
                            <button type="submit" class="btn btn-outline-light btn-sm">
                                <i class="fas fa-sign-out-alt me-1"></i>로그아웃
                            </button>
                        </form>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- 히어로 섹션 -->
    <section class="hero-section">
        <div class="container text-center">
            <h1 class="display-4 mb-4">
                <i class="fas fa-bell me-3"></i>알림 시스템
            </h1>
            <p class="lead mb-4">실시간 알림으로 중요한 정보를 놓치지 마세요</p>
            <div th:if="${!isLoggedIn}">
                <a href="/login" class="btn btn-light btn-lg me-3">
                    <i class="fas fa-sign-in-alt me-2"></i>로그인
                </a>
                <a href="/register" class="btn btn-outline-light btn-lg">
                    <i class="fas fa-user-plus me-2"></i>회원가입
                </a>
            </div>
            <div th:if="${isLoggedIn}">
                <a href="/dashboard" class="btn btn-light btn-lg me-3">
                    <i class="fas fa-tachometer-alt me-2"></i>대시보드
                </a>
                <a href="/mypage" class="btn btn-outline-light btn-lg">
                    <i class="fas fa-user me-2"></i>마이페이지
                </a>
            </div>
        </div>
    </section>

    <!-- 로그인한 사용자를 위한 시스템 현황 -->
    <section class="py-5 bg-light" th:if="${isLoggedIn}">
        <div class="container">
            <h2 class="text-center mb-5">시스템 현황</h2>
            <div class="row">
                <div class="col-md-6 mb-4">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">
                                <i class="fas fa-users me-2"></i>사용자 현황
                            </h5>
                        </div>
                        <div class="card-body">
                            <div class="row text-center">
                                <div class="col-6">
                                    <h3 class="text-primary" th:text="${totalUsers}">0</h3>
                                    <p class="text-muted">전체 사용자</p>
                                </div>
                                <div class="col-6">
                                    <h3 class="text-success" th:text="${activeUsers}">0</h3>
                                    <p class="text-muted">활성 사용자</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6 mb-4">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">
                                <i class="fas fa-clock me-2"></i>시스템 정보
                            </h5>
                        </div>
                        <div class="card-body">
                            <p><strong>현재 시간:</strong> <span th:text="${currentTime}">2024-01-01 00:00:00</span></p>
                            <p><strong>서버 상태:</strong> <span class="text-success">정상</span></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- 로그인하지 않은 사용자를 위한 안내 -->
    <section class="py-5" th:if="${!isLoggedIn}">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-md-8 text-center">
                    <h2 class="mb-4">알림 시스템을 시작하려면 로그인하세요</h2>
                    <p class="lead mb-4">로그인하면 알림 작성, 관리, 확인 등의 기능을 사용할 수 있습니다.</p>
                    <a href="/login" class="btn btn-primary btn-lg me-3">
                        <i class="fas fa-sign-in-alt me-2"></i>로그인
                    </a>
                    <a href="/register" class="btn btn-outline-primary btn-lg">
                        <i class="fas fa-user-plus me-2"></i>회원가입
                    </a>
                </div>
            </div>
        </div>
    </section>

    <!-- 기능 소개 섹션 -->
    <section class="py-5" th:if="${!isLoggedIn}">
        <div class="container">
            <h2 class="text-center mb-5">주요 기능</h2>
            <div class="row">
                <div class="col-md-4 mb-4">
                    <div class="card feature-card h-100">
                        <div class="card-body text-center">
                            <i class="fas fa-paper-plane fa-3x text-primary mb-3"></i>
                            <h5 class="card-title">알림 전송</h5>
                            <p class="card-text">개별 사용자 또는 전체 사용자에게 실시간 알림을 전송합니다.</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 mb-4">
                    <div class="card feature-card h-100">
                        <div class="card-body text-center">
                            <i class="fas fa-database fa-3x text-success mb-3"></i>
                            <h5 class="card-title">데이터 관리</h5>
                            <p class="card-text">사용자 정보와 알림 기록을 안전하게 관리합니다.</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 mb-4">
                    <div class="card feature-card h-100">
                        <div class="card-body text-center">
                            <i class="fas fa-shield-alt fa-3x text-warning mb-3"></i>
                            <h5 class="card-title">보안</h5>
                            <p class="card-text">Spring Security를 통한 안전한 인증과 권한 관리</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- 푸터 -->
    <footer class="bg-dark text-white py-4">
        <div class="container text-center">
            <p class="mb-0">&copy; 2024 알림 시스템. All rights reserved.</p>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

## 📝 3단계: 대시보드 페이지 생성

`src/main/resources/templates/dashboard.html` 파일을 생성:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${title}">대시보드 - 알림 시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .stats-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 15px;
            padding: 2rem;
            margin-bottom: 2rem;
        }
        .feature-card {
            transition: transform 0.3s ease;
            border: none;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }
        .feature-card:hover {
            transform: translateY(-5px);
        }
    </style>
</head>
<body>
    <!-- 네비게이션 바 -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="/">
                <i class="fas fa-bell me-2"></i>알림 시스템
            </a>
            
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link active" href="/dashboard">대시보드</a>
                    </li>
                    <li class="nav-item" th:if="${isAdmin}">
                        <a class="nav-link" href="/notifications/create">알림 작성</a>
                    </li>
                    <li class="nav-item" th:if="${isAdmin}">
                        <a class="nav-link" href="/notifications/list">알림 목록</a>
                    </li>
                    <li class="nav-item" th:if="${isAdmin}">
                        <a class="nav-link" href="/users/list">사용자 관리</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/mypage">마이페이지</a>
                    </li>
                </ul>
                
                <ul class="navbar-nav">
                    <li class="nav-item">
                        <span class="navbar-text me-3">
                            <i class="fas fa-user me-1"></i>
                            <span th:text="${currentUsername} + '님 환영합니다'">사용자님 환영합니다</span>
                        </span>
                    </li>
                    <li class="nav-item">
                        <form th:action="@{/logout}" method="post" class="d-inline">
                            <button type="submit" class="btn btn-outline-light btn-sm">
                                <i class="fas fa-sign-out-alt me-1"></i>로그아웃
                            </button>
                        </form>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- 대시보드 내용 -->
    <div class="container mt-4">
        <div class="row">
            <div class="col-12">
                <h1 class="mb-4">
                    <i class="fas fa-tachometer-alt me-2"></i>대시보드
                </h1>
            </div>
        </div>

        <!-- 통계 카드 -->
        <div class="row mb-4">
            <div class="col-md-3">
                <div class="stats-card text-center">
                    <i class="fas fa-users fa-2x mb-2"></i>
                    <h3 th:text="${totalUsers}">0</h3>
                    <p>전체 사용자</p>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stats-card text-center">
                    <i class="fas fa-user-check fa-2x mb-2"></i>
                    <h3 th:text="${activeUsers}">0</h3>
                    <p>활성 사용자</p>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stats-card text-center">
                    <i class="fas fa-clock fa-2x mb-2"></i>
                    <h3 th:text="${currentTime}">00:00</h3>
                    <p>현재 시간</p>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stats-card text-center">
                    <i class="fas fa-server fa-2x mb-2"></i>
                    <h3>정상</h3>
                    <p>시스템 상태</p>
                </div>
            </div>
        </div>

        <!-- 빠른 액션 -->
        <div class="row mb-4">
            <div class="col-12">
                <h3 class="mb-3">빠른 액션</h3>
            </div>
            <div class="col-md-3 mb-3" th:if="${isAdmin}">
                <a href="/notifications/create" class="btn btn-primary w-100">
                    <i class="fas fa-edit me-2"></i>알림 작성
                </a>
            </div>
            <div class="col-md-3 mb-3" th:if="${isAdmin}">
                <a href="/notifications/list" class="btn btn-success w-100">
                    <i class="fas fa-list me-2"></i>알림 목록
                </a>
            </div>
            <div class="col-md-3 mb-3" th:if="${isAdmin}">
                <a href="/users/list" class="btn btn-info w-100">
                    <i class="fas fa-users me-2"></i>사용자 관리
                </a>
            </div>
            <div class="col-md-3 mb-3">
                <a href="/mypage" class="btn btn-warning w-100">
                    <i class="fas fa-user me-2"></i>마이페이지
                </a>
            </div>
        </div>

        <!-- 시스템 정보 -->
        <div class="row">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">
                            <i class="fas fa-info-circle me-2"></i>시스템 정보
                        </h5>
                    </div>
                    <div class="card-body">
                        <p><strong>애플리케이션:</strong> 알림 시스템</p>
                        <p><strong>버전:</strong> 1.0.0</p>
                        <p><strong>데이터베이스:</strong> H2 (메모리)</p>
                        <p><strong>보안:</strong> Spring Security</p>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">
                            <i class="fas fa-user me-2"></i>사용자 정보
                        </h5>
                    </div>
                    <div class="card-body">
                        <p><strong>사용자명:</strong> <span th:text="${currentUsername}">사용자</span></p>
                        <p><strong>권한:</strong> <span th:text="${isAdmin ? '관리자' : '일반 사용자'}">일반 사용자</span></p>
                        <p><strong>로그인 시간:</strong> <span th:text="${currentTime}">2024-01-01 00:00:00</span></p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

## 📝 4단계: 프로젝트 빌드 및 테스트

이제 웹 인터페이스가 제대로 설정되었는지 테스트해요.

### Windows에서:
```cmd
# 프로젝트 빌드
gradlew build

# 애플리케이션 실행
gradlew bootRun
```

### Mac/Linux에서:
```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

## 📝 5단계: 웹 페이지 테스트

애플리케이션이 실행되면 웹 페이지를 테스트해요.

### 테스트 순서:

1. **메인 페이지**: `http://localhost:8081`
2. **로그인**: `http://localhost:8081/login`
3. **대시보드**: `http://localhost:8081/dashboard`
4. **회원가입**: `http://localhost:8081/register`

## ✅ 확인사항

웹 인터페이스가 성공적으로 설정되면:

1. **메인 페이지**: 로그인 상태에 따라 다른 내용 표시
2. **로그인 후**: 사용자명과 환영 메시지 표시
3. **대시보드**: 시스템 통계와 빠른 액션 버튼 표시
4. **반응형 디자인**: 모바일에서도 잘 보임

## 🚀 다음 단계

웹 인터페이스가 완료되면 다음 문서로 이동하세요:
👉 **[권한 기반 접근 제어 구현](./07-authorization.md)**

---

## 💡 팁

- **Thymeleaf**: 서버에서 HTML을 동적으로 생성하는 템플릿 엔진이에요
- **Bootstrap**: 웹 페이지를 예쁘게 꾸미는 CSS 프레임워크예요
- **반응형**: 화면 크기에 따라 자동으로 레이아웃이 바뀌는 디자인이에요
- **컨트롤러**: 웹 요청을 받아서 처리하는 클래스예요 