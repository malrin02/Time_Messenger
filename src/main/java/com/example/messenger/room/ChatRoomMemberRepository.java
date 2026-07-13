package com.example.messenger.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    List<ChatRoomMember> findByUserIdOrderByJoinedAtDesc(Long userId);

    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);
}