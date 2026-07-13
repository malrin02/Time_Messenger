package com.example.messenger.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChatRoomJoinByCodeRequest {

    @NotBlank
    @Size(min = 8, max = 8)
    private String inviteCode;

    @NotNull
    private Long userId;

    public String getInviteCode() {
        return inviteCode;
    }

    public Long getUserId() {
        return userId;
    }
}