package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.UserRegistrationDto;
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
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().toString())
                .disabled(!user.isActive())
                .build();
    }
    
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    public User registerUser(UserRegistrationDto registrationDto) {
        // 중복 사용자명 확인
        if (userRepository.findByUsername(registrationDto.getUsername()) != null) {
            throw new RuntimeException("이미 사용 중인 사용자명입니다.");
        }
        
        // 중복 이메일 확인
        if (userRepository.findByEmail(registrationDto.getEmail()) != null) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        
        // 비밀번호 확인
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(User.UserRole.USER);
        user.setActive(true);
        
        return userRepository.save(user);
    }
    
    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }
    
    public User updateUser(String id, User userDetails) {
        User user = getUserById(id);
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setActive(userDetails.isActive());
        return userRepository.save(user);
    }
    
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public void updateUserByUsername(String username, User userDetails) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setEmail(userDetails.getEmail());
            user.setPhoneNumber(userDetails.getPhoneNumber());
            userRepository.save(user);
        }
    }
    
    public boolean isAdmin(String username) {
        User user = userRepository.findByUsername(username);
        return user != null && user.getRole() == User.UserRole.ADMIN;
    }
} 