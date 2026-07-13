package com.example.messenger.scheduler;

import com.example.messenger.redis.MessageRedisService;
import com.example.messenger.room.ChatRoom;
import com.example.messenger.room.ChatRoomRepository;
import com.example.messenger.room.ChatRoomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpiredRoomScheduler {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRedisService messageRedisService;

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void expireRooms() {
        List<ChatRoom> expiredRooms = chatRoomRepository.findByStatusAndExpiresAtBefore(
                ChatRoomStatus.ACTIVE,
                LocalDateTime.now()
        );

        for (ChatRoom room : expiredRooms) {
            room.expire();
            messageRedisService.deleteMessages(room.getId());
        }

        if (!expiredRooms.isEmpty()) {
            System.out.println("Expired chat rooms processed: " + expiredRooms.size());
        }
    }
}