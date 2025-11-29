package com.wheats.api.order.entity;

/**
 * 주문 상태
 * - PENDING : 결제 대기(또는 준비 중)
 * - PAID    : 결제 완료
 * - CANCELLED : 주문 취소
 */
public enum OrderStatus {
    PENDING,
    PAID,
    CANCELLED
}
