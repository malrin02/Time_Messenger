package com.example.messenger.room;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChatRoomCreateRequest {

    @NotNull
    private Long ownerId;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Min(1)
    private int durationMinutes;

    public Long getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}