package com.wheats.api.mypage.dto;

import java.time.LocalDateTime;

/**
 * 마이페이지 주문 내역 목록용 DTO
 */
public class OrderHistoryItemResponse {

    private Long orderId;
    private String storeName;
    private String itemDescription; // "싸이버거 세트 외 1개" 형태
    private LocalDateTime orderDate;
    private int totalPrice;
    private String status; // "배달완료" 등

    public OrderHistoryItemResponse(Long orderId,
                                    String storeName,
                                    String itemDescription,
                                    LocalDateTime orderDate,
                                    int totalPrice,
                                    String status) {
        this.orderId = orderId;
        this.storeName = storeName;
        this.itemDescription = itemDescription;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }
}

