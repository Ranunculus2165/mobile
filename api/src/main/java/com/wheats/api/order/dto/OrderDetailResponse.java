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
    
    // 영수증 출력을 위한 추가 정보
    private String storeName;
    private String storeAddress;
    private int deliveryFee;
    private int orderAmount; // 주문 금액 (배달료 제외)
    private String userName;
    private String userEmail;
    private String receiptFlag;

    public OrderDetailResponse(Long orderId, String orderNumber, String status, int totalPrice, LocalDateTime createdAt, LocalDateTime paidAt, List<OrderItemResponse> items, String storeName, String storeAddress, int deliveryFee, int orderAmount, String userName, String userEmail, String receiptFlag) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.items = items;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.deliveryFee = deliveryFee;
        this.orderAmount = orderAmount;
        this.userName = userName;
        this.userEmail = userEmail;
        this.receiptFlag = receiptFlag;
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

    public String getStoreName() {
        return storeName;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public int getDeliveryFee() {
        return deliveryFee;
    }

    public int getOrderAmount() {
        return orderAmount;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getReceiptFlag() {
        return receiptFlag;
    }
}
