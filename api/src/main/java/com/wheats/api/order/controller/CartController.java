package com.wheats.api.order.controller;

import com.wheats.api.order.dto.CartItemRequest;
import com.wheats.api.order.dto.CartResponse;
import com.wheats.api.order.dto.UpdateCartItemQuantityRequest;
import com.wheats.api.order.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // GET /api/cart - 내 장바구니 조회
    @GetMapping
    public ResponseEntity<CartResponse> getMyCart() {
        Long userId = 1L; // TODO: 인증 붙이면 토큰에서 꺼내기

        Optional<CartResponse> cartOpt = cartService.getMyCart(userId);
        return cartOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/cart/items - 장바구니 담기
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@RequestBody CartItemRequest request) {
        Long userId = 1L;

        CartResponse response = cartService.addItem(userId, request);
        return ResponseEntity.ok(response);
    }

    // PATCH /api/cart/items/{cartItemId} - 수량 변경
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemQuantityRequest request
    ) {
        Long userId = 1L;

        CartResponse response = cartService.updateItemQuantity(userId, cartItemId, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/cart/items/{cartItemId} - 항목 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> deleteItem(@PathVariable Long cartItemId) {
        Long userId = 1L;

        CartResponse response = cartService.removeItem(userId, cartItemId);
        return ResponseEntity.ok(response);
    }
}
