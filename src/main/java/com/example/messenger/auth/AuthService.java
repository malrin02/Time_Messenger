package com.example.messenger.auth;

import com.example.messenger.user.User;
import com.example.messenger.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        User user = new User(
                request.getLoginId(),
                passwordEncoder.encode(request.getPassword())
        );

        // 닉네임 기본값: 로그인 아이디
        if (user.getNickname() == null || user.getNickname().isBlank()) {
            user.setNickname(request.getLoginId());
        }

        User savedUser = userRepository.save(user);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getLoginId(),
                savedUser.getNickname() != null && !savedUser.getNickname().isBlank()
                        ? savedUser.getNickname()
                        : savedUser.getLoginId(),
                savedUser.getProfileImageUrl()
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return new AuthResponse(
                user.getId(),
                user.getLoginId(),
                user.getNickname() != null && !user.getNickname().isBlank()
                        ? user.getNickname()
                        : user.getLoginId(),
                user.getProfileImageUrl()
        );
    }
}