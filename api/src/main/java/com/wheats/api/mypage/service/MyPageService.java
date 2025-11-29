package com.wheats.api.mypage.service;

import com.wheats.api.mypage.dto.MyPageProfileResponse;
import com.wheats.api.mypage.entity.UserEntity;
import com.wheats.api.mypage.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyPageService {

    private final UserRepository userRepository;

    public MyPageService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 현재 로그인 유저(임시로 userId 직접 전달)의 마이페이지 프로필 조회
     */
    public MyPageProfileResponse getMyProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found. id=" + userId));

        return MyPageProfileResponse.from(user);
    }
}
