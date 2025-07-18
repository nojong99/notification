package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserWebController {
    
    private final UserService userService;
    
    @GetMapping("/list")
    public String listUsers(Model model) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("userCount", users.size());
        return "user/list";
    }
    
    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "user/create";
    }
    
    @PostMapping("/create")
    public String createUser(@ModelAttribute User user) {
        userService.createUser(user);
        return "redirect:/users/list";
    }
    
    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable String id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user/edit";
    }
    
    @PostMapping("/edit/{id}")
    public String editUser(@PathVariable String id, @ModelAttribute User userDetails) {
        userService.updateUser(id, userDetails);
        return "redirect:/users/list";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return "redirect:/users/list";
    }
    
    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable String id) {
        // 관리자 권한 확인
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        User user = userService.getUserById(id);
        user.setActive(!user.isActive());
        userService.updateUser(id, user);
        return "redirect:/users/list";
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