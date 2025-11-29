package com.wheats.api.order.dto;

/**
 * 주문 상세에서 각 메뉴 라인 정보를 표현하는 DTO
 */
public class OrderItemResponse {

    private Long menuId;
    private String menuName;
    private int quantity;
    private int unitPrice;

    public OrderItemResponse(Long menuId,
                             String menuName,
                             int quantity,
                             int unitPrice) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
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
}
