package com.wheats.api.order.controller;

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
        // userId는 현재 Service 내부에서 1L 하드코딩 (MyPage / Cart와 동일)
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세/영수증 조회
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        // 인증 붙이면 토큰에서 userId 꺼내서 Service로 넘기는 형태로 확장할 수 있음
        OrderDetailResponse response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(response);
    }
}
