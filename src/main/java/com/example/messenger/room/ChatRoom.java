package com.example.messenger.room;

import com.example.messenger.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 8)
    private String inviteCode;

    @Column(nullable = false, length = 100)
    private String title;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomStatus status;

    public ChatRoom(String title, User owner, int durationMinutes, String inviteCode) {
        this.title = title;
        this.owner = owner;
        this.inviteCode = inviteCode;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusMinutes(durationMinutes);
        this.status = ChatRoomStatus.ACTIVE;
    }

    public boolean isExpired() {
        return this.status == ChatRoomStatus.EXPIRED
                || LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void expire() {
        this.status = ChatRoomStatus.EXPIRED;
    }

    public void updateTimer(int durationMinutes) {
        this.expiresAt = LocalDateTime.now().plusMinutes(durationMinutes);
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
