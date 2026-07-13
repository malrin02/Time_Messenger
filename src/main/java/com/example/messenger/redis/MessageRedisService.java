package com.example.messenger.redis;

import com.example.messenger.websocket.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveMessage(ChatMessage message, LocalDateTime expiresAt) {
        try {
            String key = getMessageKey(message.getRoomId());
            String json = objectMapper.writeValueAsString(message);

            redisTemplate.opsForList().rightPush(key, json);

            Duration ttl = Duration.between(LocalDateTime.now(), expiresAt);

            if (!ttl.isNegative() && !ttl.isZero()) {
                redisTemplate.expire(key, ttl);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지 JSON 변환 실패", e);
        }
    }

    public void updateMessagesExpiration(Long roomId, LocalDateTime expiresAt) {
        String key = getMessageKey(roomId);
        Duration ttl = Duration.between(LocalDateTime.now(), expiresAt);

        if (!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.expire(key, ttl);
        } else {
            redisTemplate.delete(key);
        }
    }

    public List<ChatMessage> getMessages(Long roomId) {
        String key = getMessageKey(roomId);

        List<String> jsonMessages = redisTemplate.opsForList().range(key, 0, -1);

        if (jsonMessages == null) {
            return List.of();
        }

        return jsonMessages.stream()
                .map(this::convertToChatMessage)
                .toList();
    }

    private ChatMessage convertToChatMessage(String json) {
        try {
            return objectMapper.readValue(json, ChatMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지 JSON 파싱 실패", e);
        }
    }

    private String getMessageKey(Long roomId) {
        return "chat:room:" + roomId + ":messages";
    }

    public void deleteMessages(Long roomId) {
        String key = getMessageKey(roomId);
        redisTemplate.delete(key);
    }
}