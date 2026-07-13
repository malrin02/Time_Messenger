package com.example.messenger.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findAllByOrderByIdDesc();

    List<ChatRoom> findByStatusAndExpiresAtBefore(
            ChatRoomStatus status,
            LocalDateTime now
    );

    Optional<ChatRoom> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}