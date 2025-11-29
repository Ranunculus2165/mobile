package com.wheats.api.order.dto;

/**
 * 주문 생성 요청
 * - 현재는 cartId만 받음
 * - 나중에 결제 수단, 요청사항, 주소 등 추가 가능
 */
public class OrderRequest {

    private Long cartId;

    public OrderRequest() {
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }
}
