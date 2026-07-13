package com.example.messenger.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateRoomTitleRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    public String getTitle() {
        return title;
    }
}
