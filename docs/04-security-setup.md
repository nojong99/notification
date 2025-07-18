# 🔐 Spring Security 인증 시스템 구현

## 📝 1단계: SecurityConfig 설정 클래스 생성

Spring Security 설정을 담당하는 클래스를 만들어요.

`src/main/java/com/example/demo/config/SecurityConfig.java` 파일을 생성:

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 공개 접근 가능한 페이지들
                .requestMatchers("/", "/login", "/register", "/h2-console/**").permitAll()
                // 정적 리소스 (CSS, JS, 이미지)
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // H2 콘솔을 위한 설정
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin()
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## 📝 2단계: UserService 생성

사용자 관련 비즈니스 로직을 처리하는 서비스를 만들어요.

`src/main/java/com/example/demo/service/UserService.java` 파일을 생성:

```java
package com.example.demo.service;

import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
    
    public User registerUser(UserRegistrationDto registrationDto) {
        // 사용자명 중복 확인
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }
        
        // 이메일 중복 확인
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        
        // 새 사용자 생성
        User user = new User(
                registrationDto.getUsername(),
                passwordEncoder.encode(registrationDto.getPassword()),
                registrationDto.getEmail(),
                registrationDto.getPhoneNumber(),
                User.UserRole.USER
        );
        
        return userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username));
    }
    
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setEmail(userDetails.getEmail());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setActive(userDetails.isActive());
        return userRepository.save(user);
    }
    
    public User updateUserByUsername(String username, User userDetails) {
        User user = getUserByUsername(username);
        user.setEmail(userDetails.getEmail());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public boolean isAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        return user != null && user.getRole() == User.UserRole.ADMIN;
    }
}
```

## 📝 3단계: AuthController 생성

로그인, 회원가입, 로그아웃을 처리하는 컨트롤러를 만들어요.

`src/main/java/com/example/demo/controller/AuthController.java` 파일을 생성:

```java
package com.example.demo.controller;

import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String register(UserRegistrationDto registrationDto, 
                         RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(registrationDto);
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
    
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }
}
```

## 📝 4단계: 로그인 페이지 템플릿 생성

로그인 페이지를 만들어요.

`src/main/resources/templates/auth/login.html` 파일을 생성:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 - 알림 시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
        }
        .login-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }
        .login-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem;
            text-align: center;
        }
        .login-form {
            padding: 2rem;
        }
        .form-control {
            border-radius: 10px;
            border: 2px solid #e9ecef;
            padding: 12px 15px;
        }
        .form-control:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
        }
        .btn-login {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            border-radius: 10px;
            padding: 12px;
            font-weight: 600;
            width: 100%;
        }
        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
        .register-link {
            text-align: center;
            margin-top: 1rem;
        }
        .alert {
            border-radius: 10px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-4">
                <div class="login-container">
                    <div class="login-header">
                        <h2><i class="fas fa-bell me-2"></i>알림 시스템</h2>
                        <p class="mb-0">로그인하여 시작하세요</p>
                    </div>
                    
                    <div class="login-form">
                        <!-- 에러 메시지 -->
                        <div th:if="${param.error}" class="alert alert-danger" role="alert">
                            <i class="fas fa-exclamation-triangle me-2"></i>
                            잘못된 사용자명 또는 비밀번호입니다.
                        </div>
                        
                        <!-- 성공 메시지 -->
                        <div th:if="${success}" class="alert alert-success" role="alert">
                            <i class="fas fa-check-circle me-2"></i>
                            <span th:text="${success}"></span>
                        </div>
                        
                        <!-- 로그아웃 메시지 -->
                        <div th:if="${param.logout}" class="alert alert-info" role="alert">
                            <i class="fas fa-info-circle me-2"></i>
                            로그아웃되었습니다.
                        </div>
                        
                        <form th:action="@{/login}" method="post">
                            <div class="mb-3">
                                <label for="username" class="form-label">
                                    <i class="fas fa-user me-2"></i>사용자명
                                </label>
                                <input type="text" class="form-control" id="username" name="username" 
                                       placeholder="사용자명을 입력하세요" required>
                            </div>
                            
                            <div class="mb-3">
                                <label for="password" class="form-label">
                                    <i class="fas fa-lock me-2"></i>비밀번호
                                </label>
                                <input type="password" class="form-control" id="password" name="password" 
                                       placeholder="비밀번호를 입력하세요" required>
                            </div>
                            
                            <button type="submit" class="btn btn-primary btn-login">
                                <i class="fas fa-sign-in-alt me-2"></i>로그인
                            </button>
                        </form>
                        
                        <div class="register-link">
                            <p class="mb-0">계정이 없으신가요? 
                                <a href="/register" class="text-decoration-none">회원가입</a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

## 📝 5단계: 회원가입 페이지 템플릿 생성

회원가입 페이지를 만들어요.

`src/main/resources/templates/auth/register.html` 파일을 생성:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입 - 알림 시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
        }
        .register-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }
        .register-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem;
            text-align: center;
        }
        .register-form {
            padding: 2rem;
        }
        .form-control {
            border-radius: 10px;
            border: 2px solid #e9ecef;
            padding: 12px 15px;
        }
        .form-control:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
        }
        .btn-register {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            border-radius: 10px;
            padding: 12px;
            font-weight: 600;
            width: 100%;
        }
        .btn-register:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
        .login-link {
            text-align: center;
            margin-top: 1rem;
        }
        .alert {
            border-radius: 10px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-5">
                <div class="register-container">
                    <div class="register-header">
                        <h2><i class="fas fa-user-plus me-2"></i>회원가입</h2>
                        <p class="mb-0">새로운 계정을 만들어보세요</p>
                    </div>
                    
                    <div class="register-form">
                        <!-- 에러 메시지 -->
                        <div th:if="${error}" class="alert alert-danger" role="alert">
                            <i class="fas fa-exclamation-triangle me-2"></i>
                            <span th:text="${error}"></span>
                        </div>
                        
                        <form th:action="@{/register}" method="post" th:object="${userRegistrationDto}">
                            <div class="mb-3">
                                <label for="username" class="form-label">
                                    <i class="fas fa-user me-2"></i>사용자명
                                </label>
                                <input type="text" class="form-control" id="username" 
                                       th:field="*{username}" placeholder="사용자명을 입력하세요" required>
                            </div>
                            
                            <div class="mb-3">
                                <label for="password" class="form-label">
                                    <i class="fas fa-lock me-2"></i>비밀번호
                                </label>
                                <input type="password" class="form-control" id="password" 
                                       th:field="*{password}" placeholder="비밀번호를 입력하세요" required>
                            </div>
                            
                            <div class="mb-3">
                                <label for="email" class="form-label">
                                    <i class="fas fa-envelope me-2"></i>이메일
                                </label>
                                <input type="email" class="form-control" id="email" 
                                       th:field="*{email}" placeholder="이메일을 입력하세요" required>
                            </div>
                            
                            <div class="mb-3">
                                <label for="phoneNumber" class="form-label">
                                    <i class="fas fa-phone me-2"></i>전화번호
                                </label>
                                <input type="tel" class="form-control" id="phoneNumber" 
                                       th:field="*{phoneNumber}" placeholder="전화번호를 입력하세요">
                            </div>
                            
                            <button type="submit" class="btn btn-primary btn-register">
                                <i class="fas fa-user-plus me-2"></i>회원가입
                            </button>
                        </form>
                        
                        <div class="login-link">
                            <p class="mb-0">이미 계정이 있으신가요? 
                                <a href="/login" class="text-decoration-none">로그인</a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

## 📝 6단계: 프로젝트 빌드 및 테스트

이제 인증 시스템이 제대로 설정되었는지 테스트해요.

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

## 📝 7단계: 로그인 테스트

애플리케이션이 실행되면 로그인 기능을 테스트해요.

### 테스트 계정들:

1. **관리자 계정**:
   - 사용자명: `admin`
   - 비밀번호: `admin123`

2. **일반 사용자 계정들**:
   - 사용자명: `user1`, 비밀번호: `password1`
   - 사용자명: `user2`, 비밀번호: `password2`
   - 사용자명: `user3`, 비밀번호: `password3`

### 테스트 순서:

1. **브라우저에서 접속**: `http://localhost:8081`
2. **로그인 페이지로 이동**: `http://localhost:8081/login`
3. **테스트 계정으로 로그인**
4. **로그인 성공 시 대시보드로 이동 확인**
5. **로그아웃 테스트**

## ✅ 확인사항

인증 시스템이 성공적으로 설정되면:

1. **빌드 성공**: `BUILD SUCCESSFUL` 메시지
2. **로그인 페이지 접속**: `http://localhost:8081/login` 접속 가능
3. **회원가입 페이지 접속**: `http://localhost:8081/register` 접속 가능
4. **로그인 성공**: 올바른 계정으로 로그인 시 대시보드로 이동
5. **권한 제어**: 로그인하지 않은 사용자는 보호된 페이지 접근 불가

## 🚨 문제 해결

### 오류 1: Spring Security 관련 오류
```
Bean of type 'UserDetailsService' could not be found
```
**해결**: `UserService`에 `@Service` 어노테이션이 제대로 설정되었는지 확인하세요.

### 오류 2: 비밀번호 인코딩 오류
```
There is no PasswordEncoder mapped for the id "null"
```
**해결**: `SecurityConfig`에 `PasswordEncoder` 빈이 제대로 설정되었는지 확인하세요.

### 오류 3: 로그인 실패
```
Invalid username or password
```
**해결**: 데이터베이스에 사용자 계정이 제대로 생성되었는지 H2 콘솔에서 확인하세요.

## 🚀 다음 단계

인증 시스템이 완료되면 다음 문서로 이동하세요:
👉 **[알림 시스템 핵심 기능 구현](./05-notification-system.md)**

---

## 💡 팁

- **Spring Security**: 웹 애플리케이션의 보안을 담당하는 프레임워크예요
- **UserDetailsService**: 사용자 정보를 로드하는 인터페이스예요
- **PasswordEncoder**: 비밀번호를 안전하게 암호화하는 도구예요
- **BCrypt**: 비밀번호 해싱 알고리즘 중 하나예요
- 로그인 실패 시 **로그 메시지**를 꼭 확인하세요 