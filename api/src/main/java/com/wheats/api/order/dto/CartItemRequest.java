package com.wheats.api.order.dto;

// POST /api/cart/items 요청 바디
public class CartItemRequest {

    private Long storeId;
    private Long menuId;
    private int quantity;

    public CartItemRequest() {
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
