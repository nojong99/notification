package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    @GetMapping
    public String myPage(Model model) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if ("anonymousUser".equals(username)) {
                return "redirect:/login";
            }
            
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return "redirect:/login";
            }
            
            // 사용자의 알림 목록 가져오기
            List<Notification> notifications = notificationService.getUserNotifications(username);
            long unreadCount = notifications.stream()
                    .filter(n -> "PENDING".equals(n.getStatus()))
                    .count();
            
            // 최근 5개 알림 (드롭다운용)
            List<Notification> recentNotifications = notifications.stream()
                    .limit(5)
                    .toList();
            
            // 최근 10개 알림 (메인 목록용)
            List<Notification> mainNotifications = notifications.stream()
                    .limit(10)
                    .toList();
            
            model.addAttribute("user", user);
            model.addAttribute("notifications", notifications);
            model.addAttribute("recentNotifications", recentNotifications);
            model.addAttribute("mainNotifications", mainNotifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("totalCount", notifications.size());
            
            return "mypage/index";
        } catch (Exception e) {
            // 오류 발생 시 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }
    }
    
    @GetMapping("/notifications")
    public String myNotifications(Model model) {
        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if ("anonymousUser".equals(username)) {
                return "redirect:/login";
            }
            
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return "redirect:/login";
            }
            
            // 사용자의 알림 목록 가져오기
            List<Notification> notifications = notificationService.getUserNotifications(username);
            long unreadCount = notifications.stream()
                    .filter(n -> "PENDING".equals(n.getStatus()))
                    .count();
            
            // 최근 5개 알림 (드롭다운용)
            List<Notification> recentNotifications = notifications.stream()
                    .limit(5)
                    .toList();
            
            model.addAttribute("user", user);
            model.addAttribute("notifications", notifications);
            model.addAttribute("recentNotifications", recentNotifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("totalCount", notifications.size());
            
            return "mypage/notifications";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
    
    @PostMapping("/notifications/{id}/read")
    @ResponseBody
    public String markAsRead(@PathVariable String id) {
        try {
            notificationService.markNotificationAsRead(id);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
    
    @PostMapping("/notifications/read-all")
    @ResponseBody
    public String markAllAsRead() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            notificationService.markAllNotificationsAsRead(username);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
    
    @GetMapping("/profile")
    public String profile(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if ("anonymousUser".equals(username)) {
                return "redirect:/login";
            }
            
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return "redirect:/login";
            }
            
            model.addAttribute("user", user);
            return "mypage/profile";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
    
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User userDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            userService.updateUserByUsername(username, userDetails);
            return "redirect:/mypage/profile?success=true";
        } catch (Exception e) {
            return "redirect:/mypage/profile?error=true";
        }
    }
    
    @GetMapping("/notifications/count")
    @ResponseBody
    public NotificationCountResponse getNotificationCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if ("anonymousUser".equals(username)) {
                return new NotificationCountResponse(0);
            }
            
            List<Notification> notifications = notificationService.getUserNotifications(username);
            long unreadCount = notifications.stream()
                    .filter(n -> "PENDING".equals(n.getStatus()))
                    .count();
            
            return new NotificationCountResponse(unreadCount);
        } catch (Exception e) {
            return new NotificationCountResponse(0);
        }
    }
    
    // 알림 개수 응답 DTO
    public static class NotificationCountResponse {
        private long unreadCount;
        
        public NotificationCountResponse(long unreadCount) {
            this.unreadCount = unreadCount;
        }
        
        public long getUnreadCount() {
            return unreadCount;
        }
        
        public void setUnreadCount(long unreadCount) {
            this.unreadCount = unreadCount;
        }
    }
} 