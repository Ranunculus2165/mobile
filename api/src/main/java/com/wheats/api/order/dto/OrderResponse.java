package com.wheats.api.order.dto;

import java.time.LocalDateTime;

/**
 * 주문 생성(결제 포함) 결과를 클라이언트로 내려줄 DTO
 */
public class OrderResponse {

    private Long orderId;
    private String orderNumber;
    private int totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public OrderResponse(Long orderId,
                         String orderNumber,
                         int totalPrice,
                         String status,
                         LocalDateTime createdAt,
                         LocalDateTime paidAt) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
