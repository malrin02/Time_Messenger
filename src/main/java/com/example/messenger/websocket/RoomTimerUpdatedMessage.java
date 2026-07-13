package com.example.messenger.websocket;

import java.time.LocalDateTime;

public class RoomTimerUpdatedMessage {

    private final String type;
    private final Long roomId;
    private final Integer durationMinutes;
    private final LocalDateTime expiresAt;

    public RoomTimerUpdatedMessage(String type, Long roomId, Integer durationMinutes, LocalDateTime expiresAt) {
        this.type = type;
        this.roomId = roomId;
        this.durationMinutes = durationMinutes;
        this.expiresAt = expiresAt;
    }

    public String getType() {
        return type;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
