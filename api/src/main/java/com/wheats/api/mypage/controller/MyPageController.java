package com.wheats.api.mypage.controller;

import com.wheats.api.mypage.dto.MyPageProfileResponse;
import com.wheats.api.mypage.dto.MyPageResponse;
import com.wheats.api.mypage.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    /**
     * 내 프로필 조회
     * 지금은 OAuth 안 붙였으니까 userId=1L로 하드코딩.
     * 나중에 Security / OAuth 붙이면 여기만 교체.
     */
    @GetMapping("/me")
    public ResponseEntity<MyPageProfileResponse> getMyProfile() {
        Long userId = 1L; // TODO: 인증 붙이면 토큰에서 꺼내기

        MyPageProfileResponse response = myPageService.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 마이페이지 전체 정보 조회 (프로필 + 주문 내역)
     * GET /api/users/me/page
     */
    @GetMapping("/me/page")
    public ResponseEntity<MyPageResponse> getMyPage() {
        Long userId = 1L; // TODO: 인증 붙이면 토큰에서 꺼내기

        MyPageResponse response = myPageService.getMyPage(userId);
        return ResponseEntity.ok(response);
    }
}
