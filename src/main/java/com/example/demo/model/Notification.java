package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Data
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    
    @Indexed
    @Field("user_id")
    private String userId;
    
    private String title;
    private String message;
    private String type; // INFO, WARNING, ERROR
    private String status = "PENDING"; // PENDING, SENT, FAILED, READ
    
    @Field("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Field("sent_at")
    private LocalDateTime sentAt;
} 