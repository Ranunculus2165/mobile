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
        try {
            Long userId = AuthContext.getCurrentUserId();
            System.out.println("🛒 장바구니 조회 요청: userId=" + userId);

            Optional<CartResponse> cartOpt = cartService.getMyCart(userId);
            return cartOpt
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            System.err.println("❌ 인증 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            System.err.println("❌ 장바구니 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/cart/items - 장바구니 담기
    // 쿼리 파라미터: force=true일 경우 다른 가게 장바구니가 있어도 강제로 새 장바구니 생성
    @PostMapping("/items")
    public ResponseEntity<?> addItem(
            @RequestBody CartItemRequest request,
            @RequestParam(value = "force", defaultValue = "false") boolean force) {
        try {
            Long userId = AuthContext.getCurrentUserId();
            System.out.println("🛒 장바구니 아이템 추가 요청: userId=" + userId + ", storeId=" + request.getStoreId() + ", menuId=" + request.getMenuId());

            CartResponse response = cartService.addItem(userId, request, force);
            System.out.println("✅ 장바구니 아이템 추가 성공");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // 인증 관련 오류
            System.err.println("❌ 인증 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "인증 오류");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (CartConflictException e) {
            // 409 Conflict: 다른 가게의 장바구니가 존재할 때 현재 장바구니 정보와 함께 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getReason());
            errorResponse.put("existingCart", e.getExistingCart());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            // 기타 예외
            System.err.println("❌ 장바구니 아이템 추가 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "서버 오류");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
