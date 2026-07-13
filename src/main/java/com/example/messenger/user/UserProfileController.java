package com.example.messenger.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserRepository userRepository;

    @Value("${app.upload.profile-dir:uploads/profiles}")
    private String profileUploadDir;

    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/{userId}/profile-image")
    public ResponseEntity<UserProfileImageResponse> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("image") MultipartFile image
    ) throws Exception {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String originalFilename = image.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String savedFileName = userId + "_" + UUID.randomUUID() + extension;

        Path uploadPath = Paths.get(profileUploadDir)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(savedFileName);
        image.transferTo(filePath.toFile());

        String profileImageUrl = "/uploads/profiles/" + savedFileName;

        user.setProfileImageUrl(profileImageUrl);
        userRepository.save(user);

        return ResponseEntity.ok(
                new UserProfileImageResponse(
                        user.getId(),
                        user.getLoginId(),
                        profileImageUrl
                )
        );
    }

    @PatchMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileUpdateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (request.nickname() != null && !request.nickname().trim().isEmpty()) {
            user.setNickname(request.nickname().trim());
        }

        userRepository.save(user);

        return ResponseEntity.ok(
                new UserProfileResponse(
                        user.getId(),
                        user.getLoginId(),
                        user.getNickname(),
                        user.getProfileImageUrl()
                )
        );
    }
}