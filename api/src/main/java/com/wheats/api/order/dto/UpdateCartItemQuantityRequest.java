package com.wheats.api.order.dto;

// PATCH /api/cart/items/{cartItemId} 요청 바디
public class UpdateCartItemQuantityRequest {

    private int quantity;

    public UpdateCartItemQuantityRequest() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
