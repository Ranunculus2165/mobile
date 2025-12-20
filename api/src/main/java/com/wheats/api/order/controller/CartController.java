package com.wheats.api.order.controller;

import com.wheats.api.auth.util.AuthContext;
import com.wheats.api.order.dto.CartItemRequest;
import com.wheats.api.order.dto.CartResponse;
import com.wheats.api.order.dto.UpdateCartItemQuantityRequest;
import com.wheats.api.order.exception.CartConflictException;
import com.wheats.api.order.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
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
        Long userId = AuthContext.getCurrentUserId();

        Optional<CartResponse> cartOpt = cartService.getMyCart(userId);
        return cartOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/cart/items - 장바구니 담기
    // 쿼리 파라미터: force=true일 경우 다른 가게 장바구니가 있어도 강제로 새 장바구니 생성
    @PostMapping("/items")
    public ResponseEntity<?> addItem(
            @RequestBody CartItemRequest request,
            @RequestParam(value = "force", defaultValue = "false") boolean force) {
        Long userId = AuthContext.getCurrentUserId();

        try {
            CartResponse response = cartService.addItem(userId, request, force);
            return ResponseEntity.ok(response);
        } catch (CartConflictException e) {
            // 409 Conflict: 다른 가게의 장바구니가 존재할 때 현재 장바구니 정보와 함께 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getReason());
            errorResponse.put("existingCart", e.getExistingCart());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    // PATCH /api/cart/items/{cartItemId} - 수량 변경
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemQuantityRequest request
    ) {
        Long userId = AuthContext.getCurrentUserId();

        CartResponse response = cartService.updateItemQuantity(userId, cartItemId, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/cart/items/{cartItemId} - 항목 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> deleteItem(@PathVariable Long cartItemId) {
        Long userId = AuthContext.getCurrentUserId();

        CartResponse response = cartService.removeItem(userId, cartItemId);
        return ResponseEntity.ok(response);
    }
}
