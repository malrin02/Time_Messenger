package com.example.messenger.room;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateRoomTimerRequest {

    @NotNull
    @Min(1)
    private Integer durationMinutes;

    public Integer getDurationMinutes() {
        return durationMinutes;
    }
}
