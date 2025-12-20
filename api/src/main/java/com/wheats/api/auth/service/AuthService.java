package com.wheats.api.auth.service;

import com.wheats.api.auth.dto.LoginRequest;
import com.wheats.api.auth.dto.LoginResponse;
import com.wheats.api.auth.util.JwtUtil;
import com.wheats.api.mypage.entity.UserEntity;
import com.wheats.api.mypage.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 이메일로 로그인 (비밀번호 검증 없음)
     * @param request 로그인 요청 (이메일만 포함)
     * @return 로그인 응답 (토큰, 사용자 정보)
     * @throws IllegalArgumentException 사용자를 찾을 수 없을 때
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 사용자 조회
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. email: " + request.getEmail()));

        // 2. 비밀번호 검증 없음 (이메일만 확인)

        // 3. JWT 토큰 생성 (userId와 role 포함)
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        // 4. LoginResponse 반환
        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()  // CONSUMER, MERCHANT, ADMIN
        );
    }
}
