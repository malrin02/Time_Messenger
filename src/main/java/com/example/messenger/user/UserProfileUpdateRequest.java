package com.example.messenger.user;

public record UserProfileUpdateRequest(
        String nickname,
        String name,
        String phone
) {
}