package com.example.messenger.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ChatMessage {

    private String type;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderProfileImageUrl;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public ChatMessage() {
    }

    public ChatMessage(
            String type,
            Long roomId,
            Long senderId,
            String senderName,
            String senderProfileImageUrl,
            String content,
            LocalDateTime createdAt
    ) {
        this.type = type;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfileImageUrl = senderProfileImageUrl;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderProfileImageUrl() {
        return senderProfileImageUrl;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setSenderProfileImageUrl(String senderProfileImageUrl) {
        this.senderProfileImageUrl = senderProfileImageUrl;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}