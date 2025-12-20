package com.wheats.api.order.controller;

import com.wheats.api.auth.util.AuthContext;
import com.wheats.api.order.dto.OrderDetailResponse;
import com.wheats.api.order.dto.OrderRequest;
import com.wheats.api.order.dto.OrderResponse;
import com.wheats.api.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 주문 생성 (장바구니 기반, 결제까지 한 번에)
     * POST /api/orders
     *
     * body: { "cartId": 1 }
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        Long userId = AuthContext.getCurrentUserId();
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세/영수증 조회
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        Long userId = AuthContext.getCurrentUserId();
        OrderDetailResponse response = orderService.getOrderDetail(userId, orderId);
        return ResponseEntity.ok(response);
    }
}
