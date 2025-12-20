package com.wheats.api.order.exception;

import com.wheats.api.order.dto.CartResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CartConflictException extends ResponseStatusException {
    
    private final CartResponse existingCart;

    public CartConflictException(CartResponse existingCart) {
        super(HttpStatus.CONFLICT, "다른 가게의 장바구니가 이미 존재합니다.");
        this.existingCart = existingCart;
    }

    public CartResponse getExistingCart() {
        return existingCart;
    }
}
