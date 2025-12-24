package com.wheats.api.mypage.controller;

import com.wheats.api.auth.util.AuthContext;
import com.wheats.api.mypage.dto.MyPageProfileResponse;
import com.wheats.api.mypage.dto.MyPageResponse;
import com.wheats.api.mypage.dto.OrderHistoryItemResponse;
import com.wheats.api.mypage.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    /**
     * 내 프로필 조회
     */
    @GetMapping("/me")
    public ResponseEntity<MyPageProfileResponse> getMyProfile() {
        Long userId = AuthContext.getCurrentUserId();

        MyPageProfileResponse response = myPageService.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 마이페이지 전체 정보 조회 (프로필 + 주문 내역)
     * GET /api/users/me/page
     */
    @GetMapping("/me/page")
    public ResponseEntity<MyPageResponse> getMyPage() {
        Long userId = AuthContext.getCurrentUserId();

        MyPageResponse response = myPageService.getMyPage(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 주문 내역 조회
     * GET /api/users/me/orders
     */
    @GetMapping("/me/orders")
    public ResponseEntity<List<OrderHistoryItemResponse>> getAllOrderHistory() {
        Long userId = AuthContext.getCurrentUserId();

        List<OrderHistoryItemResponse> response = myPageService.getAllOrderHistory(userId);
        return ResponseEntity.ok(response);
    }
}
