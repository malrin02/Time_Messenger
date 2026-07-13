package com.example.messenger.user;

public record UserProfileResponse(
        Long userId,
        String loginId,
        String nickname,
        String profileImageUrl
) {
}