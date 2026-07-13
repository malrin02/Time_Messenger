package com.example.messenger.websocket;

public class ChatMessageRequest {

    private Long roomId;
    private Long senderId;
    private String content;

    public Long getRoomId() {
        return roomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }
}