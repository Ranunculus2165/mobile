package com.wheats.api.order.dto;

public class CartItemResponse {

    private Long cartItemId;
    private Long menuId;
    private String menuName;
    private int quantity;
    private int unitPrice;
    private int linePrice;

    public CartItemResponse(Long cartItemId,
                            Long menuId,
                            String menuName,
                            int quantity,
                            int unitPrice,
                            int linePrice) {
        this.cartItemId = cartItemId;
        this.menuId = menuId;
        this.menuName = menuName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.linePrice = linePrice;
    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public int getLinePrice() {
        return linePrice;
    }
}
