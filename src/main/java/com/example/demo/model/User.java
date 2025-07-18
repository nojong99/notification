package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String username;
    
    @Indexed(unique = true)
    private String email;
    
    private String phoneNumber;
    private String password;
    
    @Field("role")
    private UserRole role = UserRole.USER;
    
    private boolean active = true;
    
    @Field("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum UserRole {
        ADMIN, USER
    }
} 