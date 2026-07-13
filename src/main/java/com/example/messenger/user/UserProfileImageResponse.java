package com.example.messenger.user;

public record UserProfileImageResponse(
        Long userId,
        String loginId,
        String profileImageUrl
) {
}