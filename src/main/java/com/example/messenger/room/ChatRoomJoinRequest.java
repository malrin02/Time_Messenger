package com.example.messenger.room;

import jakarta.validation.constraints.NotNull;

public class ChatRoomJoinRequest {

    @NotNull
    private Long userId;

    public Long getUserId() {
        return userId;
    }
}