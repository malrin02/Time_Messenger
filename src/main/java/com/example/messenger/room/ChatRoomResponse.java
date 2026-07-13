package com.example.messenger.room;

import java.time.LocalDateTime;

public class ChatRoomResponse {

    private final Long roomId;
    private final String title;
    private final Long ownerId;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final ChatRoomStatus status;
    private final String inviteCode;
    private final String imageUrl;

    public ChatRoomResponse(ChatRoom room) {
        this.roomId = room.getId();
        this.title = room.getTitle();
        this.ownerId = room.getOwner().getId();
        this.createdAt = room.getCreatedAt();
        this.expiresAt = room.getExpiresAt();
        this.status = room.getStatus();
        this.inviteCode = room.getInviteCode();
        this.imageUrl = room.getImageUrl();
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getTitle() {
        return title;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public ChatRoomStatus getStatus() {
        return status;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
