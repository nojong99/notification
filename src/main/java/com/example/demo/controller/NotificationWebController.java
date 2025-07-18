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
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationWebController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    @GetMapping("/create")
    public String createNotificationForm(Model model) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        model.addAttribute("notification", new Notification());
        model.addAttribute("users", notificationService.getAllUsers());
        return "notification/create";
    }
    
    @PostMapping("/create")
    public String createNotification(@ModelAttribute Notification notification, 
                                   @RequestParam(required = false) String sendToAll) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        if ("true".equals(sendToAll)) {
            notificationService.sendNotificationToAllUsers(
                notification.getTitle(), 
                notification.getMessage(), 
                notification.getType()
            );
        } else {
            notificationService.sendNotificationToUser(
                notification.getUserId(), 
                notification.getTitle(), 
                notification.getMessage(), 
                notification.getType()
            );
        }
        return "redirect:/notifications/list";
    }
    
    @GetMapping("/list")
    public String listNotifications(Model model) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        List<Notification> notifications = notificationService.getAllNotifications();
        model.addAttribute("notifications", notifications);
        return "notification/list";
    }
    
    @GetMapping("/user/{userId}")
    public String userNotifications(@PathVariable String userId, Model model) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        model.addAttribute("notifications", notifications);
        model.addAttribute("userId", userId);
        return "notification/user-list";
    }
    
    @PostMapping("/{id}/resend")
    public String resendNotification(@PathVariable String id) {
        notificationService.resendNotification(id);
        return "redirect:/notifications/list";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteNotification(@PathVariable String id) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        notificationService.deleteNotification(id);
        return "redirect:/notifications/list";
    }
    
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return userService.isAdmin(username);
        }
        return false;
    }
} 