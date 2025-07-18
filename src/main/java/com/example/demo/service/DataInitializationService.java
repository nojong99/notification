package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
    }
    
    private void initializeUsers() {
        if (userRepository.count() == 0) {
            log.info("기본 사용자 계정을 생성합니다...");
            
            // 기본 관리자 계정 생성 (비밀번호: admin123)
            User admin = createUser("admin", "admin@example.com", "010-1234-5678", "admin123", User.UserRole.ADMIN);
            userRepository.save(admin);
            log.info("기본 관리자 계정이 생성되었습니다: {}", admin.getUsername());
            
            // 테스트 사용자 계정 생성 (비밀번호: user123)
            User user1 = createUser("user1", "user1@example.com", "010-1111-1111", "user123", User.UserRole.USER);
            userRepository.save(user1);
            log.info("테스트 사용자 계정이 생성되었습니다: {}", user1.getUsername());
            
            User user2 = createUser("user2", "user2@example.com", "010-2222-2222", "user123", User.UserRole.USER);
            userRepository.save(user2);
            log.info("테스트 사용자 계정이 생성되었습니다: {}", user2.getUsername());
        } else {
            log.info("사용자 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
        }
    }
    
    private User createUser(String username, String email, String phoneNumber, String password, User.UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        return user;
    }
} 