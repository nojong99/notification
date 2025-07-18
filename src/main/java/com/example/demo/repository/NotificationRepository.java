package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserId(String userId);
    List<Notification> findByStatus(String status);
    List<Notification> findByUserIdAndStatus(String userId, String status);
    
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<Notification> findAllByOrderByCreatedAtDesc();
    
    // 상태별 카운트 메서드들
    long countByStatus(String status);
} 