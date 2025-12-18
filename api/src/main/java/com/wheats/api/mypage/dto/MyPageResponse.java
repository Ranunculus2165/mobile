package com.wheats.api.mypage.dto;

import java.util.List;

/**
 * 마이페이지 전체 응답 DTO (프로필 + 주문 내역)
 */
public class MyPageResponse {

    private String name;
    private String email;
    private List<OrderHistoryItemResponse> orderHistory;

    public MyPageResponse(String name, String email, List<OrderHistoryItemResponse> orderHistory) {
        this.name = name;
        this.email = email;
        this.orderHistory = orderHistory;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<OrderHistoryItemResponse> getOrderHistory() {
        return orderHistory;
    }
}

