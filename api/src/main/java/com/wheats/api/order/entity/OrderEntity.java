package com.wheats.api.order.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 번호 (ORD-타임스탬프 형태 등)
    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    // FK: users.id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // FK: stores.id
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    // FK: carts.id (어떤 장바구니에서 주문이 생성되었는지 추적)
    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    // DB에서 DEFAULT CURRENT_TIMESTAMP 처리
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // 결제 완료 시간 (모바일에서 주문+결제를 한 번에 처리하므로, 여기 now() 넣을 예정)
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // CTF용 영수증 플래그 (지금은 로직에서 안 씀, 나중에 채우면 됨)
    @Column(name = "receipt_flag")
    private String receiptFlag;

    // JPA 기본 생성자
    protected OrderEntity() {
    }

    // 우리가 실제로 사용할 생성자
    public OrderEntity(Long userId,
                       Long storeId,
                       Long cartId,
                       String orderNumber,
                       OrderStatus status,
                       int totalPrice) {

        this.userId = userId;
        this.storeId = storeId;
        this.cartId = cartId;
        this.orderNumber = orderNumber;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    // ===== Getter =====

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public Long getCartId() {
        return cartId;
    }

    public OrderStatus getStatus() {
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

    public String getReceiptFlag() {
        return receiptFlag;
    }

    // ===== Setter (상태 변경에 필요한 것만) =====

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void setReceiptFlag(String receiptFlag) {
        this.receiptFlag = receiptFlag;
    }
}
