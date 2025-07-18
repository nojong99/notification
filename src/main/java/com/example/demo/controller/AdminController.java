package com.example.demo.controller;

import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        // 알림 통계
        NotificationService.NotificationStats stats = notificationService.getNotificationStats();
        model.addAttribute("stats", stats);
        
        // 대기 중인 알림 목록
        model.addAttribute("pendingNotifications", notificationService.getNotificationsByStatus("PENDING"));
        
        // 최근 알림 목록
        model.addAttribute("recentNotifications", notificationService.getAllNotifications());
        
        return "admin/dashboard";
    }
    
    @GetMapping("/notifications")
    public String adminNotifications(Model model) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        model.addAttribute("notifications", notificationService.getAllNotifications());
        return "admin/notifications";
    }
    
    @PostMapping("/notifications/{id}/process")
    public String processNotification(@PathVariable String id, RedirectAttributes redirectAttributes) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        try {
            notificationService.processNotification(id);
            redirectAttributes.addFlashAttribute("success", "알림이 성공적으로 처리되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "알림 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/notifications";
    }
    
    @PostMapping("/notifications/{id}/resend")
    public String resendNotification(@PathVariable String id, RedirectAttributes redirectAttributes) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        try {
            notificationService.resendNotification(id);
            redirectAttributes.addFlashAttribute("success", "알림이 재전송되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "알림 재전송 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/notifications";
    }
    
    @PostMapping("/notifications/{id}/delete")
    public String deleteNotification(@PathVariable String id, RedirectAttributes redirectAttributes) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        try {
            notificationService.deleteNotification(id);
            redirectAttributes.addFlashAttribute("success", "알림이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "알림 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/admin/notifications";
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