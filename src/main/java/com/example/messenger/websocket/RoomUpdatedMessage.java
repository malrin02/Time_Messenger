package com.example.messenger.websocket;

public class RoomUpdatedMessage {
    private final String type;
    private final Long roomId;
    private final String title;
    private final String imageUrl;

    public RoomUpdatedMessage(String type, Long roomId, String title, String imageUrl) {
        this.type = type;
        this.roomId = roomId;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
