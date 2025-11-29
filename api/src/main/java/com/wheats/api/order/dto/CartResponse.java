package com.wheats.api.order.dto;

import java.util.List;

public class CartResponse {

    private Long cartId;
    private Long storeId;
    private String storeName;
    private List<CartItemResponse> items;
    private int totalPrice;

    public CartResponse(Long cartId,
                        Long storeId,
                        String storeName,
                        List<CartItemResponse> items,
                        int totalPrice) {
        this.cartId = cartId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public Long getCartId() {
        return cartId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public int getTotalPrice() {
        return totalPrice;
    }
}
