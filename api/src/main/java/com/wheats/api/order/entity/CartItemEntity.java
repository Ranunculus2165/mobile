package com.wheats.api.order.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CartItemStatus status = CartItemStatus.ACTIVE;

    protected CartItemEntity() {
    }

    public CartItemEntity(Long cartId, Long menuId, int quantity) {
        this.cartId = cartId;
        this.menuId = menuId;
        this.quantity = quantity;
        this.status = CartItemStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Long getCartId() {
        return cartId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public CartItemStatus getStatus() {
        return status;
    }

    public void setStatus(CartItemStatus status) {
        this.status = status;
    }
}
