package com.example.demo.dto;

import lombok.Data;

@Data
public class NotificationMessage {
    private String userId;
    private String title;
    private String message;
    private String type; // EMAIL, SMS, PUSH
    private String email;
    private String phoneNumber;
} 