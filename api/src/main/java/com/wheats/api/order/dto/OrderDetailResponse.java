package com.wheats.api.order.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 상세/영수증 응답 DTO
 */
public class OrderDetailResponse {

    private Long orderId;
    private String orderNumber;
    private String status;
    private int totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private List<OrderItemResponse> items;

    public OrderDetailResponse(Long orderId,
                               String orderNumber,
                               String status,
                               int totalPrice,
                               LocalDateTime createdAt,
                               LocalDateTime paidAt,
                               List<OrderItemResponse> items) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.items = items;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getStatus() {
        return status;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}
