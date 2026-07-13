package com.example.messenger.room;

import com.example.messenger.common.MessageResponse;
import com.example.messenger.websocket.ChatMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.messenger.room.ChatRoomEnterResponse;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Value("${app.upload.room-dir:uploads/rooms}")
    private String roomUploadDir;

    // 채팅방 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatRoomResponse createRoom(@Valid @RequestBody ChatRoomCreateRequest request) {
        return chatRoomService.createRoom(request);
    }

    // 보안 코드로 채팅방 입장
    @PostMapping("/join-by-code")
    public ChatRoomResponse joinRoomByCode(
            @Valid @RequestBody ChatRoomJoinByCodeRequest request
    ) {
        return chatRoomService.joinRoomByCode(request);
    }

    // roomId로 채팅방 입장
    @PostMapping("/{roomId}/join")
    public ChatRoomResponse joinRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody ChatRoomJoinRequest request
    ) {
        return chatRoomService.joinRoom(roomId, request);
    }

    // 전체 채팅방 목록 조회
    @GetMapping
    public List<ChatRoomResponse> getRooms() {
        return chatRoomService.getRooms();
    }

    // 내가 참가한 채팅방 목록 조회
    @GetMapping("/my")
    public List<ChatRoomResponse> getMyRooms(@RequestParam Long userId) {
        return chatRoomService.getMyRooms(userId);
    }

    // 특정 채팅방 단건 조회
    @GetMapping("/{roomId}")
    public ChatRoomResponse getRoom(@PathVariable Long roomId) {
        return chatRoomService.getRoom(roomId);
    }

    // 자동 삭제 타이머 변경
    @PatchMapping("/{roomId}/timer")
    public ChatRoomResponse updateRoomTimer(
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateRoomTimerRequest request
    ) {
        return chatRoomService.updateRoomTimer(roomId, request.getDurationMinutes());
    }

    @PatchMapping("/{roomId}/title")
    public ChatRoomResponse updateRoomTitle(
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateRoomTitleRequest request
    ) {
        return chatRoomService.updateRoomTitle(roomId, request.getTitle());
    }

    @PostMapping("/{roomId}/image")
    public ChatRoomResponse uploadRoomImage(
            @PathVariable Long roomId,
            @RequestParam("image") MultipartFile image
    ) throws Exception {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
        }

        String originalFilename = image.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String savedFileName = roomId + "_" + UUID.randomUUID() + extension;

        Path uploadPath = Paths.get(roomUploadDir)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(savedFileName);
        image.transferTo(filePath.toFile());

        return chatRoomService.updateRoomImage(
                roomId,
                "/uploads/rooms/" + savedFileName
        );
    }

    @GetMapping("/{roomId}/enter")
    public ChatRoomEnterResponse enterRoom(
            @PathVariable Long roomId,
            @RequestParam Long userId
    ) {
        return chatRoomService.enterRoom(roomId, userId);
    }

    // 특정 채팅방의 이전 메시지 조회
    @GetMapping("/{roomId}/messages")
    public List<ChatMessage> getRoomMessages(
            @PathVariable Long roomId,
            @RequestParam Long userId
    ) {
        return chatRoomService.getRoomMessages(roomId, userId);
    }

    // 채팅방 나가기
    @DeleteMapping("/{roomId}/members")
    public MessageResponse leaveRoom(
            @PathVariable Long roomId,
            @RequestParam Long userId
    ) {
        chatRoomService.leaveRoom(roomId, userId);
        return new MessageResponse("채팅방 나가기 성공");
    }

}
