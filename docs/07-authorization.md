# 🔒 권한 기반 접근 제어 구현

## 📝 1단계: 권한 체크 메서드 추가

컨트롤러들에 관리자 권한 체크를 추가해요.

### NotificationWebController에 권한 체크 추가

```java
// 각 메서드에 관리자 권한 체크 추가
private boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
        String username = authentication.getName();
        return userService.isAdmin(username);
    }
    return false;
}
```

### UserWebController에 권한 체크 추가

```java
// 사용자 관리 기능에 관리자 권한 체크 추가
@GetMapping("/list")
public String listUsers(Model model) {
    // 관리자 권한 확인
    if (!isAdmin()) {
        return "redirect:/login";
    }
    // ... 나머지 코드
}
```

## 📝 2단계: 템플릿에서 권한별 메뉴 표시

Thymeleaf 템플릿에서 사용자 권한에 따라 다른 메뉴를 표시해요.

```html
<!-- 관리자만 볼 수 있는 메뉴 -->
<li class="nav-item" th:if="${isAdmin}">
    <a class="nav-link" href="/notifications/create">알림 작성</a>
</li>
<li class="nav-item" th:if="${isAdmin}">
    <a class="nav-link" href="/notifications/list">알림 목록</a>
</li>
<li class="nav-item" th:if="${isAdmin}">
    <a class="nav-link" href="/users/list">사용자 관리</a>
</li>
```

## 📝 3단계: 일반 사용자용 마이페이지 구현

일반 사용자가 자신의 알림만 볼 수 있는 마이페이지를 만들어요.

### MyPageController 생성

```java
@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    @GetMapping
    public String myPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userService.getUserByUsername(username);
        List<Notification> notifications = notificationService.getUserNotifications(username);
        
        model.addAttribute("user", user);
        model.addAttribute("notifications", notifications);
        model.addAttribute("notificationCount", notifications.size());
        
        return "mypage/index";
    }
}
```

## 📝 4단계: 권한별 페이지 접근 제어

Spring Security 설정에서 권한별 접근을 제어해요.

```java
.authorizeHttpRequests(authz -> authz
    // 공개 접근 가능한 페이지들
    .requestMatchers("/", "/login", "/register", "/h2-console/**").permitAll()
    // 관리자만 접근 가능한 페이지들
    .requestMatchers("/notifications/create", "/notifications/list", "/users/list").hasRole("ADMIN")
    // 나머지는 인증 필요
    .anyRequest().authenticated()
)
```

## ✅ 확인사항

권한 기반 접근 제어가 성공적으로 설정되면:

1. **관리자**: 모든 기능 접근 가능
2. **일반 사용자**: 자신의 알림만 확인 가능
3. **권한 없는 페이지**: 접근 시 로그인 페이지로 리다이렉트
4. **메뉴 표시**: 권한에 따라 다른 메뉴 표시

## 🚀 다음 단계

권한 기반 접근 제어가 완료되면 다음 문서로 이동하세요:
👉 **[문제 해결 및 디버깅](./08-troubleshooting.md)** 