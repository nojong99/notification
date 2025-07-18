package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
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
    private final NotificationService notificationService;
    
    public WebController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }
    
    @GetMapping("/")
    public String index(Model model) {
        // 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = null;
        boolean isLoggedIn = false;
        boolean isAdmin = false;
        long unreadCount = 0;
        
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            currentUsername = authentication.getName();
            isLoggedIn = true;
            isAdmin = userService.isAdmin(currentUsername);
            
            // 알림 개수 가져오기
            List<com.example.demo.model.Notification> notifications = notificationService.getUserNotifications(currentUsername);
            unreadCount = notifications.stream()
                    .filter(n -> "PENDING".equals(n.getStatus().toString()))
                    .count();
        }
        
        // 실제 데이터베이스에서 가져오기
        List<User> users = userService.getAllUsers();
        List<User> activeUsers = userService.getActiveUsers();
        
        model.addAttribute("title", "알림 시스템");
        model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("activeUsers", activeUsers.size());
        model.addAttribute("users", activeUsers); // 활성 사용자 목록
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("apiEndpoints", new String[]{
            "/api/test/health - 서버 상태 확인",
            "/api/test/hello - 인사말",
            "/api/test/info - 서버 정보",
            "/api/notifications/send/user/{userId} - 사용자별 알림 전송",
            "/api/notifications/send/all - 전체 알림 전송",
            "/api/notifications/user/{userId} - 사용자별 알림 조회"
        });
        return "index";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 실제 데이터베이스에서 가져오기
        List<User> users = userService.getAllUsers();
        List<User> activeUsers = userService.getActiveUsers();
        
        model.addAttribute("title", "알림 시스템 대시보드");
        model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("activeUsers", activeUsers.size());
        model.addAttribute("users", activeUsers);
        return "dashboard";
    }
    
    @GetMapping("/test")
    public String testPage(Model model) {
        model.addAttribute("title", "API 테스트");
        return "test";
    }
    
    @GetMapping("/debug/users")
    public String debugUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("userCount", users.size());
        return "debug/users";
    }
} 