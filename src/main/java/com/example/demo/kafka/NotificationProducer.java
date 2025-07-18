package com.example.demo.kafka;

import com.example.demo.dto.NotificationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String TOPIC = "notifications";
    
    public void sendNotification(NotificationMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, message.getUserId(), messageJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("메시지가 성공적으로 전송되었습니다. topic={}, partition={}, offset={}", 
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("메시지 전송 실패: {}", ex.getMessage());
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 오류: {}", e.getMessage());
        }
    }
} 