package com.example.messenger.websocket;

public class RoomExpiredMessage {

    private final String type;
    private final Long roomId;
    private final String message;

    public RoomExpiredMessage(String type, Long roomId, String message) {
        this.type = type;
        this.roomId = roomId;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getMessage() {
        return message;
    }
}