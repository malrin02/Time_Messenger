package com.example.messenger.auth;

public class AuthResponse {

    private final Long userId;
    private final String loginId;
    private final String nickname;
    private final String profileImageUrl;
    private final String message;

    public AuthResponse(
            Long userId,
            String loginId,
            String nickname,
            String profileImageUrl
    ) {
        this.userId = userId;
        this.loginId = loginId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.message = null;
    }

    public AuthResponse(
            Long userId,
            String loginId,
            String nickname,
            String profileImageUrl,
            String message
    ) {
        this.userId = userId;
        this.loginId = loginId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getMessage() {
        return message;
    }
}